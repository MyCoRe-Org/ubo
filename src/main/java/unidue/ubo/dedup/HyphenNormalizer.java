/**
 * Copyright (c) 2016 Duisburg-Essen University Library
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 **/

package unidue.ubo.dedup;

/**
 * Normalizes the different variants of hyphens in a given input text to a simple "minus" character.  
 * 
 * @author Frank L\u00FCtzenkirchen 
 **/
public class HyphenNormalizer {

    private final static char HYPHEN_NORM = '-';

    private char[] HYPHEN_VARIANTS = { '\u002D', '\u2010', '\u2011', '\u2012', '\u2013', '\u2015', '\u2212', '\u2E3B',
        '\uFE58', '\uFE63' };

    /**
     * Normalizes the different variants of hyphens in a given input text to a simple "minus" character.  
     **/
    public String normalize(String input) {
        for (char hypenVariant : HYPHEN_VARIANTS)
            input = input.replace(hypenVariant, HYPHEN_NORM);
        return input;
    }
}
