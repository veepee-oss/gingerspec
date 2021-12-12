package com.privalia.qa.lookups;

import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import org.apache.commons.text.lookup.StringLookup;

/**
 * Evaluates the given Mathematical expression using the exp4j library
 * @see <a href="https://www.objecthunter.net/exp4j/#Evaluating_an_expression">exp4j</a>
 */
public class MathLookup implements StringLookup {

    @Override
    public String lookup(String key) {
        if (key == null) {
            return null;
        }

        Expression e = new ExpressionBuilder(key).build();
        Double result = e.evaluate();
        return result.toString();
    }
}
