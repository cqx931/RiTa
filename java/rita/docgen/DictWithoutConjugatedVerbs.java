
package rita.docgen;

import java.io.FileWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.SortedMap;
import java.util.TreeMap;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import rita.RiTa;

public class DictWithoutConjugatedVerbs {
  
  static String WORD_FILE = "js/src/list.txt";
  static String DICT_FILE = "js/src/rita_dict.js";
  static String SYLL_FILE = "js/src/cmudict-0.7b";
  static String OUTPUT_FILE = "/tmp/rita_dict_new.js"; // change me
  static HashMap<String, String[]> rdata = parseRiTaDict(
      RiTa.loadStrings(DICT_FILE));

  static String header, footer;
  static int matches = 0;
  static int deleted = 0;
  static int entries = 0;
  static int ignored = 0;
  static int added = 0;
  static int moreItems = 0;

  public static void main(String[] args) {
//    SortedMap newdata = tidyUp(rdata);
    SortedMap newdata = generate(rdata);
    System.out.println("--------------------");
    System.out.println("Original entries : " + rdata.size());
    System.out.println("Matches : " + matches);
    System.out.println("Ignore : " + ignored);
    System.out.println("Added : " + added + "," + moreItems);
    System.out.println("Deleted : " + deleted);
    System.out.println("Entries left : " + entries);

    System.out.println("");

    System.out
	.println("Wrote: " + writeToFile(OUTPUT_FILE, mapToString(newdata)));
  }

  private static SortedMap tidyUp(HashMap rdata) {
    String[] words = RiTa.loadStrings(WORD_FILE);
    String[] cwords = RiTa.loadStrings(SYLL_FILE);    
    HashMap<String, String> cdata = parseCMU(cwords);
    SortedMap<String, String> newdata = new TreeMap<String, String>();
    
//    for (int i = 0; i < words.length; i++) {
//      String word = words[i].toLowerCase();
//      String cmuPhones = cdata.get(word);
//      if(cmuPhones == null){
//	 System.err.println(word);
//	 ignored++;
//      }else{
////	System.out.println(word + " " + cmuPhones);
//	 cmuPhones = cmuPhones.replaceAll("[02]", "");
//	 newdata.put(word, "['"+cmuPhones+"','vb']");
//	added++;
//      }
//      matches++;
//    }
  
    for (Iterator<String> it = rdata.keySet().iterator(); it.hasNext();) {
      String word = it.next();
      String[] rval = (String[]) rdata.get(word);
      String phones = rval[0];
      String pos = rval[1];
      
      if (hasTag(pos, "nns")) {
	System.err.println(word + " " + pos);
      }
      
    newdata.put(word, "['" + phones + "','" + pos + "']");

    }

    return newdata;

  }

  private static boolean hasTag(String pos, String tag) {
    String[] tags = pos.split(" ");
    for (int i = 0; i < tags.length; i++) {
      if (tags[i].equals(tag))
	return true;
    }
    return false;
  }

  private static SortedMap generate(HashMap rdata) {
    SortedMap<String, String> newdata = new TreeMap<String, String>();
    SortedMap<String, String> newEntryList = new TreeMap<String, String>();

    for (Iterator<String> it = rdata.keySet().iterator(); it.hasNext();) {
      String word = it.next();
      String[] rval = (String[]) rdata.get(word);
      String phones = rval[0];
      String pos = rval[1];
      
      if(pos.contains("vbg") || pos.contains("vbn") || pos.contains("vbd")){
	matches ++;
	System.out.println("[DELETE]" + word + " " + pos);
      }
	
    }
    // Total entries needed: 720
    // verb base with more entries 120
    // But how to know the exact vb??And the right syll?

    return newdata;
  }

  private static HashMap<String, String[]> parseRiTaDict(String[] words) {
    HashMap<String, String[]> rita = new HashMap<String, String[]>();
    header = words[0].trim();
    footer = words[words.length - 1].trim();
    for (int i = 1; i < words.length - 1; i++) {
      String[] parts = words[i].trim().split(":");
      if (parts.length != 2)
	throw new RuntimeException("Bad line: " + words[i]);
      String word = parts[0].replaceAll("'", "").trim();
      String value = parts[1].replaceAll("['\\[\\]]", "").replaceAll(",$", "");
      parts = value.split(",");
      String phones = parts[0].replaceAll("^ ", "");
      String sylls = parts[1];
      rita.put(word, new String[] { phones, sylls });
    }
    return rita;
  }

  static String writeToFile(String fname, String content) {
    try {
      FileWriter fw = new FileWriter(fname);
      fw.write((content == null) ? "" : content);
      fw.flush();
      fw.close();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    return fname;
  }

  private static boolean endsWithItemFromList(String inputString,
      String[] items) {
    for (int i = 0; i < items.length; i++) {
      if (inputString.endsWith(items[i])) {
	return true;
      }
    }
    return false;
  }

  private static String mapToString(SortedMap newdata) {
    
    StringBuilder sb = new StringBuilder();
    sb.append(header+"\n");
    for (Iterator<String> it = newdata.keySet().iterator(); it.hasNext();) {
      String word = it.next();
      sb.append("'"+word+"':"+newdata.get(word));
      sb.append(it.hasNext() ? ",\n" : "\n");
    }
    sb.append(footer);
    return sb.toString();
  }

  public static boolean canRemove(String word, String pos) {

    // only matches words whose tags consist only of vb*
    if (pos.matches("^vb[a-z]( vb[a-z])*$")) {
      matches++;
      String vb = getVerbBaseForm(word, pos);
      if (vb == "") {
	System.err
	    .println("[Keep] Can't get the vb of word:" + word + " " + pos);
	/**
	 * Case 1: vbp Case 2: stand - stood
	 **/
	return false;
      }

      if (rdata.containsKey(vb))
	return true;
      else {
	// deal with vb that should end with "e" Ex: removed -> remove
	// any better solution?
	if (!vb.endsWith("e") && rdata.containsKey(vb + "e"))
	  return true;
      }
      System.out.println("[Keep] " + word + " " + vb + " " + pos);
    }

    return false;
  }

  private static HashMap<String, String> parseCMU(String[] words) {
    
    HashMap<String, String> cmu = new HashMap<String, String>();
    for (int i = 0; i < words.length; i++) {
      String[] parts = words[i].toLowerCase().trim().split("  +");
      if (parts.length != 2)
	throw new RuntimeException("Bad line: "+words[i]);
      String word = parts[0].trim(), value = parts[1].trim();
      String sylls = value.replaceAll(" - ", "/").replaceAll(" ", "-").replaceAll("/", " ");
      cmu.put(word, sylls);
    }
    return cmu;
  }
  
  public static String getVerbBaseForm(String word, String pos) {
    String vb = "";
    String stem = rita.RiTa.stem(word, RiTa.PORTER);
    vb = stem;

    // //
    // String[] endings = { "ent", "ion", "er", "ate", "ize" };
    //
    // String[] exceptions = new String[endings.length * 3];
    // for (int i = 0; i < endings.length; i++) {
    // String ending = endings[i];
    // if (endings[i].endsWith("e"))
    // ending = endings[i].replaceAll("e$", "");
    // exceptions[3 * i] = ending + "s";
    // exceptions[3 * i + 1] = ending + "ing";
    // exceptions[3 * i + 2] = ending + "ed";
    // }
    //
    // if (endsWithItemFromList(word, exceptions)) vb = word.replaceAll("ed$",
    // "");

    if (word.endsWith("ating") || word.endsWith("izing"))
      vb = word.replaceAll("ing$", "");
    if (word.endsWith("ates") || word.endsWith("izes"))
      vb = word.replaceAll("es$", "");
    if (word.endsWith("ated") || word.endsWith("ized"))
      vb = word.replaceAll("ed$", "");

    if (word.endsWith("ents") || word.endsWith("ions") || word.endsWith("ers"))
      vb = word.replaceAll("s$", "");

    if (word.endsWith("ented") || word.endsWith("ioned")
	|| word.endsWith("ered"))
      vb = word.replaceAll("ed$", "");

    if (word.endsWith("enting") || word.endsWith("ioning")
	|| word.endsWith("ering"))
      vb = word.replaceAll("ing$", "");

    // beautify -> beautified
    if (vb.endsWith("i"))
      vb = vb.replaceAll("i$", "y");

    // if(pos.contains("vbp"))
    // System.out.println("[VBP] " + word + " " + vb + " " + pos);

    return vb;
  }

}
