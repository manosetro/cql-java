// $Id: CQLBooleanNode.java,v 1.5 2002-10-30 09:19:26 mike Exp $

package org.z3950.zing.cql;


/**
 * Represents a boolean node in a CQL parse-tree.
 * ###
 *
 * @version	$Id: CQLBooleanNode.java,v 1.5 2002-10-30 09:19:26 mike Exp $
 */
public abstract class CQLBooleanNode extends CQLNode {
    protected CQLNode left;
    protected CQLNode right;

    abstract String op();

    String toXCQL(int level) {
	return (indent(level) + "<triple>\n" +
		indent(level+1) + "<boolean>" + op() + "</boolean>\n" +
		left.toXCQL(level+1) +
		right.toXCQL(level+1) +
		indent(level) + "</triple>\n");
    }

    String toCQL() {
	// ### We don't always need parens around the operands
	return "(" + left.toCQL() + ") " + op() + " (" + right.toCQL() + ")";
    }
}
