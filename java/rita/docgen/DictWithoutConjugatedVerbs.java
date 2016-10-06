
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

  static String DICT_FILE = "js/src/rita_dict.js";
  static String OUTPUT_FILE = "/tmp/rita_dict_new.js"; // change me
  static HashMap<String, String[]> rdata = parseRiTaDict(RiTa.loadStrings(DICT_FILE));

  static String header, footer;
  static int matches = 0;
  static int deleted = 0;
  static int entries = 0;
  static int ignored = 0;
  static int added = 0;

  static Matcher dublicateLetter;

  public static void main(String[] args) {

    SortedMap newdata = generate(rdata);
    System.out.println("--------------------");
    System.out.println("Original entries : " + rdata.size());
    System.out.println("Matches : " + matches);
    System.out.println("Ignore : " + ignored);
    System.out.println("Added : " + added);
    System.out.println("Deleted : " + deleted);
    System.out.println("Entries left : " + entries);

    System.out.println("");

    System.out
	.println("Wrote: " + writeToFile(OUTPUT_FILE, mapToString(newdata)));
  }

  private static SortedMap generate(HashMap rdata) {
    SortedMap<String, String> newdata = new TreeMap<String, String>();
    SortedMap<String, String> newEntryList = new TreeMap<String, String>();

    for (Iterator<String> it = rdata.keySet().iterator(); it.hasNext();) {
      String word = it.next();
      String[] rval = (String[]) rdata.get(word);
      String phones = rval[0];
      String pos = rval[1];

      // Step 1 Add Verb Base Form
      // all entries that contains vb* tags, no vb in tags Total: 8584
      if (pos.contains("vb") && !pos.contains("vb ") && !pos.endsWith("vb")) {
	matches++;

	String vb = getVerbBaseForm(word, pos);
	if (vb.equals(word)) {
	  // System.err.println("[IGNORE!] Irregular Verb:" + word + " " + pos);
	  ignored++;
	} else {

	  if (rdata.containsKey(vb) || rdata.containsKey(vb + "e")
	      || rdata.containsKey(vb + vb.charAt(vb.length() - 1))) {
	    // 3 Cases vb || vb+ 'e' || vb with last letter repeated

	  } else {
	    System.out.println("[NEED ENTRY!]:" + vb + " " + word + " " + pos);
	    if (newEntryList.containsKey(vb)) {
	      String newValue = newEntryList.get(vb) + "|" + word + " " + pos;
	      newEntryList.replace(vb, newValue);
	    } else {
	      newEntryList.put(vb, word + " " + pos);
	      added++;
	      // how to know the exact vb?? the right syll?
	    }

	  }
	}
      }

      // TO DO : Step 2 Delete
      //
      // if (!isConjugatedVerb(rdata, word, rval[1])) {
      // newdata.put(word, "['" + phones + "','" + pos + "']");
      // entries++;
      // } else {
      // // System.out.println("[Delete] " + word + ":" + pos );
      // deleted++;
      // }

    }

    // System.out.println(mapToString(newEntryList));

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

 private static void printList(SortedMap list) {
    for (Iterator<String> i = list.keySet().iterator(); i.hasNext();) {
      String key = i.next();
      String value = (String) list.get(i);
      System.out.println(key + " => " + value);
    }
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

  private static boolean endsWithItemFromList(String inputString, String[] items) {
    for (int i = 0; i < items.length; i++) {
      if (inputString.endsWith(items[i])) {
	return true;
      }
    }
    return false;
  }

  private static String mapToString(SortedMap newdata) {

    StringBuilder sb = new StringBuilder();
    sb.append(header + "\n");
    for (Iterator<String> it = newdata.keySet().iterator(); it.hasNext();) {
      String word = it.next();
      sb.append("'" + word + "':" + newdata.get(word));
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

  public static String getVerbBaseForm(String word, String pos) {
    String vb = "";
    String stem = rita.RiTa.stem(word, RiTa.PORTER);
    vb = stem;

    if (word.endsWith("ating") || word.endsWith("izing"))
      vb = word.replaceAll("ing$", "e");
    if (word.endsWith("ates") || word.endsWith("izes"))
      vb = word.replaceAll("s$", "");
    if (word.endsWith("ated") || word.endsWith("ized"))
      vb = word.replaceAll("d$", "");

//    // ent, tion,er
//    String[] endings = { "ent", "ion", "er", "ate", "ize" };
//
//    String[] exceptions = new String[endings.length * 3];
//    for (int i = 0; i < endings.length; i++) {
//      String ending = endings[i];
//      if (endings[i].endsWith("e"))
//	ending = endings[i].replaceAll("e$", "");
//      exceptions[3 * i] = ending + "s";
//      exceptions[3 * i + 1] = ending + "ing";
//      exceptions[3 * i + 2] = ending + "ed";
//    }
//
//    if (endsWithItemFromList(word, exceptions)) vb = word.replaceAll("ed$", "");

      if (word.endsWith("ents") || word.endsWith("ions")
	  || word.endsWith("ers"))
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

    return vb;
  }

}
