// $Id: ModifierSet.java,v 1.2 2002-11-02 01:20:42 mike Exp $

package org.z3950.zing.cql;
import java.util.Vector;
import java.lang.StringBuffer;

/**
 * Represents a base String and a set of modifier Strings.
 * <P>
 * This class is used as a workhorse delegate by both CQLRelation and
 * CQLProxNode - two functionally very separate classes that happen to
 * require the same data structures and functionality.
 *
 * @version $Id: ModifierSet.java,v 1.2 2002-11-02 01:20:42 mike Exp $
 */
public class ModifierSet {
    String base;
    Vector modifiers;

    public ModifierSet(String base) {
	this.base = base;
	modifiers = new Vector();
    }

    public void addModifier(String type, String value) {
	Vector modifier = new Vector();
	modifier.add(type);
	modifier.add(value);
	modifiers.add(modifier);
    }

    public Vector[] getModifiers() {
	int n = modifiers.size();
	Vector[] res = new Vector[n];
	for (int i = 0; i < n; i++) {
	    res[i] = (Vector) modifiers.get(i);
	}

	return res;
    }

    public String toXCQL(int level, String topLevelElement) {
	StringBuffer buf = new StringBuffer();
	buf.append (Utils.indent(level) + "<" + topLevelElement + ">\n" +
		    Utils.indent(level+1) + "<value>" + Utils.xq(base) +
		    "</value>\n");
	Vector[] mods = getModifiers();
	if (mods.length > 0) {
	    buf.append(Utils.indent(level+1) + "<modifiers>\n");
	    for (int i = 0; i < mods.length; i++) {
		Vector modifier = mods[i];
		buf.append(Utils.indent(level+2)).
		    append("<modifier>");
		if (modifier.get(0) != null)
		    buf.append("<type>").
			append(Utils.xq((String) modifier.get(0))).
			append("</type>");
		buf.append("<value>").
		    append(Utils.xq((String) modifier.get(1))).
		    append("</value>");
		buf.append("</modifier>\n");
	    }
	    buf.append(Utils.indent(level+1) + "</modifiers>\n");
	}
	buf.append(Utils.indent(level) + "</" + topLevelElement + ">\n");
	return buf.toString();
    }

    public String toCQL() {
	StringBuffer buf = new StringBuffer(base);
	Vector[] mods = getModifiers();
	for (int i = 0; i < mods.length; i++) {
	    buf.append("/").append(mods[i].get(1));
	}

	return buf.toString();
    }

    public static void main(String[] args) {
	if (args.length < 1) {
	    System.err.println("Usage: ModifierSet <base> [<type> <name>]...");
	    System.exit(1);
	}

	ModifierSet res = new ModifierSet(args[0]);
	for (int i = 1; i < args.length; i += 2) {
	    res.addModifier(args[i], args[i+1]);
	}

	System.out.println(res.toCQL());
    }
}
