// $Id: CQLPrefixNode.java,v 1.10 2007-07-03 16:40:11 mike Exp $

package org.z3950.zing.cql;
import java.lang.String;
import java.util.Properties;
import java.util.Vector;


/**
 * Represents a prefix node in a CQL parse-tree.
 *
 * @version	$Id: CQLPrefixNode.java,v 1.10 2007-07-03 16:40:11 mike Exp $
 */
public class CQLPrefixNode extends CQLNode {
    /**
     * The prefix definition that governs the subtree.
     */
    public CQLPrefix prefix;

    /**
     * The root of a parse-tree representing the part of the query
     * that is governed by this prefix definition.
     */ 
    public CQLNode subtree;

    /**
     * Creates a new CQLPrefixNode inducing a mapping from the
     * specified index-set name to the specified identifier across
     * the specified subtree.
     */
    public CQLPrefixNode(String name, String identifier, CQLNode subtree) {
	this.prefix = new CQLPrefix(name, identifier);
	this.subtree = subtree;
    }

    public String toXCQL(int level, Vector<CQLPrefix> prefixes,
			 Vector<ModifierSet> sortkeys) {
	Vector<CQLPrefix> tmp = (prefixes == null ?
				 new Vector<CQLPrefix>() :
				 new Vector<CQLPrefix>(prefixes));
	tmp.add(prefix);
	return subtree.toXCQL(level, tmp, sortkeys);
    }

    public String toCQL() {
	// ### We don't always need parens around the subtree
	if (prefix.name == null) {
	    return ">\"" + prefix.identifier + "\" " +
		"(" + subtree.toCQL() + ")";
	} else {
	    return ">" + prefix.name + "=\"" + prefix.identifier + "\" " +
		"(" + subtree.toCQL() + ")";
	}
    }

    public String toPQF(Properties config) throws PQFTranslationException {
	// Prefixes and their identifiers don't actually play any role
	// in PQF translation, since the meanings of the indexes,
	// including their prefixes if any, are instead wired into
	// `config'.
	return subtree.toPQF(config);
    }

    public byte[] toType1BER(Properties config) throws PQFTranslationException {
	// See comment on toPQF()
	return subtree.toType1BER(config);
    }
}