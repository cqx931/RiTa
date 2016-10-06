package rita.test;

import static rita.support.QUnitStubs.*;
import org.junit.Test;

import rita.RiLexicon;
import rita.docgen.*;

public class IsConjugatedVerbTest {
  
  @Test
  public void testgetVerbBaseForm() {
    
    //getvb tests
    String[] list = { 
	"abounded", "vbd",
	"abounding", "vbg",
	"abounds", "vbz",
    };
    
    for (int i = 0; i < list.length; i+=2) {
      String result = DictWithoutConjugatedVerbs.getVerbBaseForm(list[i],list[i+1]);
      System.out.println(list[i] + ":" + result);
      equal(result, "abound");
    }
    
    String[] list1 = { 
	"accompanying", "vbg jj",
	"accompanied", "vbn vbd",
	"accompanies", "vbz",
    };
    
    for (int i = 0; i < list1.length; i+=2) {
      String result = DictWithoutConjugatedVerbs.getVerbBaseForm(list1[i],list1[i+1]);
      System.out.println(list1[i] + ":" + result);
      equal(result, "accompany");
    }
    
    String[] list2 = { 
	"abmitted", "vbd vbn jj",
	"abmitting", "vbg",
    };
    
    for (int i = 0; i < list2.length; i+=2) {
      String result = DictWithoutConjugatedVerbs.getVerbBaseForm(list2[i],list2[i+1]);
      System.out.println(list2[i] + ":" + result);
      equal(result, "abmit");
    }
    
  }
  
  @Test
  public void testIsConjugatedVerb() {
    
    String[] remove = { 
	"abounded", "vbd",
	"abounding", "vbg",
	"accentuated", "vbn vbd" ,
    };
    
    String[] keep = { 
	"abuse", "nn vb vbp" ,
	"accent", "nn vb" ,
	"accented", "vbn jj",
	"accents", "nns vbz" ,
	"accentuate", "vb" ,
	"accept", "vb vbp" ,
	"accounting", "nn vbg jj" ,
	"abused", "vbn jj vbd" ,
    };
    
    for (int i = 0; i < remove.length; i+=2) {
      System.out.println(remove[i] + ":" + remove[i+1] + " " + DictWithoutConjugatedVerbs.canRemove(remove[i] , remove[i+1]));
      ok( DictWithoutConjugatedVerbs.canRemove(remove[i] , remove[i+1]));
    }
    
    for (int i = 0; i < keep.length; i+=2) {
      System.out.println(keep[i] + ":" + keep[i+1] + " " + DictWithoutConjugatedVerbs.canRemove(keep[i] , keep[i+1]));
      ok( !DictWithoutConjugatedVerbs.canRemove(keep[i] , keep[i+1]));
    }
    
    String[] removeSet = { 
	"abound", "vb",
	"abounded", "vbd",
	"abounding", "vbg",
	"abounds", "vbz",
       };
    
    String[] removeSet1 = { 
	"accompany", "vb vbp",
	"accompanied", "vbn vbd",
	"accompanies", "vbz",
       };
    
    String[] keepSet = { 
	"abbetted", "vbn vbd",
	"abetting", "vbg",
       };
    
    for (int i = 2; i < removeSet.length; i+=2) {
      System.out.println(removeSet[i] + ":" + removeSet[i+1] + " " + DictWithoutConjugatedVerbs.canRemove(removeSet[i] , removeSet[i+1]));
      ok( DictWithoutConjugatedVerbs.canRemove(removeSet[i] , removeSet[i+1]));
    }
    
    for (int i = 2; i < removeSet1.length; i+=2) {
      System.out.println(removeSet1[i] + ":" + removeSet1[i+1] + " " + DictWithoutConjugatedVerbs.canRemove(removeSet1[i], removeSet1[i+1]));
      ok( DictWithoutConjugatedVerbs.canRemove(removeSet1[i], removeSet1[i+1]));
    }
    
    for (int i = 0; i < keepSet.length; i+=2) {
      System.out.println(keepSet[i] + ":" + keepSet[i+1] + " " + DictWithoutConjugatedVerbs.canRemove(keepSet[i] , keepSet[i+1]));
      ok( !DictWithoutConjugatedVerbs.canRemove(keepSet[i] , keepSet[i+1]));
    }
    
  }
  
  public static void main(String[] args) {
    
    IsConjugatedVerbTest test = new IsConjugatedVerbTest();

  }
}
