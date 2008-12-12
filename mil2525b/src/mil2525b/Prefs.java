package mil2525b;

import java.util.*;
import java.util.prefs.Preferences;

/**
 * Handy class for working with local operating system
 * to save preferences. Objects would likely create
 * a Preferences field and reuse the object, despite
 * the fact that I try to cached the Preferences object here.
 * <pre>private Preferences prefs = Prefs.get(this);</pre>
 * 
 * @author robert.harder
 */
public class Prefs {

    
    private static Map<Object,Preferences> cachedPrefs = 
            Collections.synchronizedMap(new WeakHashMap<Object,Preferences>());
    
    
    /**
     * Return a Preferences object based on the object, usually <tt>this</tt>.
     * @param root
     * @return preferences
     */
    public static Preferences get( Object root ){
        Preferences prefs = cachedPrefs.get(root);
        if( prefs == null ){
            prefs = createPrefs(root);
            cachedPrefs.put(root,prefs);
        }   // end if: null
        return prefs;
    }
    
    
    private static Preferences createPrefs( Object root ){
        String startingPoint = "";
        StackTraceElement[] trace = Thread.currentThread().getStackTrace();
        for( int i = 0; i < trace.length; i++ ){
            if( trace[i].getClassName().startsWith("java.awt.") ){
                if( i > 0 ){
                    startingPoint = trace[i-1].getClassName();
                    break; // Out of for loop
                }
            }
        }
        Preferences prefs = Preferences.userRoot()
                .node(startingPoint.replaceAll("\\.", "/"))
                .node(root.getClass().getName().replaceAll("\\.", "/"));
        return prefs;
    }   // end createPrefs
    
    
}
