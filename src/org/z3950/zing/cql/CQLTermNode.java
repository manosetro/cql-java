// $Id: CQLTermNode.java,v 1.13 2002-12-06 12:35:42 mike Exp $

package org.z3950.zing.cql;
import java.util.Properties;
import java.util.Vector;


/**
 * Represents a terminal node in a CQL parse-tree.
 * A term node consists of the term String itself, together with,
 * optionally, a qualifier string and a relation.  Neither or both of
 * these must be provided - you can't have a qualifier without a
 * relation or vice versa.
 *
 * @version	$Id: CQLTermNode.java,v 1.13 2002-12-06 12:35:42 mike Exp $
 */
public class CQLTermNode extends CQLNode {
    private String qualifier;
    private CQLRelation relation;
    private String term;

    /**
     * Creates a new term node with the specified <TT>qualifier</TT>,
     * <TT>relation</TT> and <TT>term</TT>.  The first two may be
     * <TT>null</TT>, but the <TT>term</TT> may not.
     */
    public CQLTermNode(String qualifier, CQLRelation relation, String term) {
	this.qualifier = qualifier;
	this.relation = relation;
	this.term = term;
    }

    public String getQualifier() { return qualifier; }
    public CQLRelation getRelation() { return relation; }
    public String getTerm() { return term; }

    public String toXCQL(int level, Vector prefixes) {
	return (indent(level) + "<searchClause>\n" +
		renderPrefixes(level+1, prefixes) +
		indent(level+1) + "<index>" + xq(qualifier) + "</index>\n" +
		relation.toXCQL(level+1, new Vector()) +
		indent(level+1) + "<term>" + xq(term) + "</term>\n" +
		indent(level) + "</searchClause>\n");
    }

    public String toCQL() {
	String quotedQualifier = maybeQuote(qualifier);
	String quotedTerm = maybeQuote(term);
	String res = quotedTerm;

	if (!qualifier.equalsIgnoreCase("srw.serverChoice")) {
	    // ### We don't always need spaces around `relation'.
	    res = quotedQualifier + " " + relation.toCQL() + " " + quotedTerm;
	}

	return res;
    }

    // ### Interaction between this and its callers is not good as
    //	regards truncation of the term and generation of truncation
    //	attributes.  Change the interface to fix this.
    private Vector getAttrs(Properties config) throws PQFTranslationException {
	Vector attrs = new Vector();

	// Do this first so that if any other truncation or
	// completeness attributes are generated, they "overwrite"
	// those specified here.
	//
	//  ###	This approach relies on an unpleasant detail of Index
	//	Data's (admittedly definitive) implementation of PQF,
	//	and should not relied upon.
	//
	String attr = config.getProperty("always");
	if (attr != null)
	    attrs.add(attr);

	attr = config.getProperty("qualifier." + qualifier);
	if (attr == null)
	    throw new UnknownQualifierException(qualifier);
	attrs.add(attr);

	String rel = relation.getBase();
	if (rel.equals("=")) {
	    rel = "eq";
	} else if (rel.equals("<=")) {
	    rel = "le";
	} else if (rel.equals(">=")) {
	    rel = "ge";
	}
	// ### Handling "any" and "all" properly would involve breaking
	// the string down into a bunch of individual words and ORring
	// or ANDing them together.  Another day.
	attr = config.getProperty("relation." + rel);
	if (attr == null)
	    throw new UnknownRelationException(rel);
	attrs.add(attr);

	String[] mods = relation.getModifiers();
	for (int i = 0; i < mods.length; i++) {
	    attr = config.getProperty("relationModifier." + mods[i]);
	    if (attr == null)
		throw new UnknownRelationModifierException(mods[i]);
	    attrs.add(attr);
	}

	String pos = "any";
	String text = term;
	if (text.length() > 0 && text.substring(0, 1).equals("^")) {
	    text = text.substring(1);
	    pos = "first";
	}
	// ### add back the last-in-field stuff
	attr = config.getProperty("position." + pos);
	if (attr == null)
	    throw new UnknownPositionException(pos);
	attrs.add(attr);

	attr = config.getProperty("structure." + rel);
	if (attr == null)
	    attr = config.getProperty("structure.*");
	attrs.add(attr);

	return attrs;
    }

    public String toPQF(Properties config) throws PQFTranslationException {
	Vector attrs = getAttrs(config);

	String attr, s = "";
	for (int i = 0; i < attrs.size(); i++) {
	    attr = (String) attrs.get(i);
	    s += "@attr " + Utils.replaceString(attr, " ", " @attr ") + " ";
	}

	String text = term;
	if (text.length() > 0 && text.substring(0, 1).equals("^"))
	    text = text.substring(1);
	return s + maybeQuote(text);
    }

    static String maybeQuote(String str) {
	// There _must_ be a better way to make this test ...
	if (str.length() == 0 ||
	    str.indexOf('"') != -1 ||
	    str.indexOf(' ') != -1 ||
	    str.indexOf('\t') != -1 ||
	    str.indexOf('=') != -1 ||
	    str.indexOf('<') != -1 ||
	    str.indexOf('>') != -1 ||
	    str.indexOf('/') != -1 ||
	    str.indexOf('(') != -1 ||
	    str.indexOf(')') != -1) {
	    str = '"' + Utils.replaceString(str, "\"", "\\\"") + '"';
	}

	return str;
    }

    /**
     * ### Document this!
     */
    public byte[] toType1(Properties config) throws PQFTranslationException {
	String text = term;
	if (text.length() > 0 && text.substring(0, 1).equals("^"))
	    text = text.substring(1);
	String attr, attrList, term = maybeQuote(text);
	System.out.println("in CQLTermNode.toType101(): PQF=" + toPQF(config));
	byte[] operand = new byte[text.length()+100];
	int i, j, offset, type, value;
	offset = putTag(CONTEXT, 0, CONSTRUCTED, operand, 0); // op
	operand[offset++]=(byte)(0x80&0xff); // indefinite length
	offset = putTag(CONTEXT, 102, CONSTRUCTED, operand, offset); // AttributesPlusTerm
	operand[offset++] = (byte)(0x80&0xff); // indefinite length
	offset = putTag(CONTEXT, 44, CONSTRUCTED, operand, offset); // AttributeList
	operand[offset++] = (byte)(0x80&0xff); // indefinite length
	offset = putTag(UNIVERSAL, SEQUENCE, CONSTRUCTED, operand, offset);
	operand[offset++] = (byte)(0x80&0xff);

	Vector attrs = getAttrs(config);
	for(i = 0; i < attrs.size(); i++) {
	    attrList = (String) attrs.get(i);
	    java.util.StringTokenizer st =
		new java.util.StringTokenizer(attrList);
	    while (st.hasMoreTokens()) {
		attr = st.nextToken();
		j = attr.indexOf('=');
		offset = putTag(CONTEXT, 120, PRIMITIVE, operand, offset);
		type = Integer.parseInt(attr.substring(0, j));
		offset = putLen(numLen(type), operand, offset);
		offset = putNum(type, operand, offset);

		offset = putTag(CONTEXT, 121, PRIMITIVE, operand, offset);
		value = Integer.parseInt(attr.substring(j+1));
		offset = putLen(numLen(value), operand, offset);
		offset = putNum(value, operand, offset);
	    }
	}
	operand[offset++] = 0x00; // end of SEQUENCE
	operand[offset++] = 0x00;
	operand[offset++] = 0x00; // end of AttributeList
	operand[offset++] = 0x00;

	offset = putTag(CONTEXT, 45, PRIMITIVE, operand, offset); // general Term
	byte[] t = term.getBytes();
	offset = putLen(t.length, operand, offset);
	System.arraycopy(t, 0, operand, offset, t.length);
	offset += t.length;

	operand[offset++] = 0x00; // end of AttributesPlusTerm
	operand[offset++] = 0x00;
	operand[offset++] = 0x00; // end of Operand
	operand[offset++] = 0x00;
	byte[] o = new byte[offset];
	System.arraycopy(operand, 0, o, 0, offset);
	return o;
    }
}
