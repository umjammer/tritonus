/*
 * AlsaCtlTestCase.java
 */
/*
 *  Copyright (c) 2001 - 2002 by Matthias Pfisterer
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package org.tritonus.test.alsa;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;

import org.junit.jupiter.api.Test;
import org.tritonus.lowlevel.alsa.AlsaCtl;

import static java.lang.System.getLogger;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class AlsaCtlTestCase {

    private static final Logger logger = getLogger(AlsaCtlTestCase.class.getName());

    private static final String CARD_NAME_FOR_INDEX_TEST = "LIFE";

    @Test
    public void testGetCards() {
        int[] cards = AlsaCtl.getCards();
        assertNotNull(cards);
        assertEquals(1, cards.length);
        assertTrue(cards[0] >= 0);
    }

    @Test
    public void testLoadCards() {
        int[] cards = AlsaCtl.getCards();
        for (int anCard : cards) {
            int error = AlsaCtl.loadCard(anCard);
            assertTrue(error >= 0);
        }
    }

    @Test
    public void testGetIndex() {
        int index = AlsaCtl.getCardIndex(CARD_NAME_FOR_INDEX_TEST);
        logger.log(Level.DEBUG, "card index: " + index);
        assertTrue(index >= 0);
        int[] cards = AlsaCtl.getCards();
        logger.log(Level.DEBUG, "card index: " + cards[0]);
        assertEquals(index, cards[0]);
    }

    @Test
    public void testGetNames() {
        int[] cards = AlsaCtl.getCards();
        String name = AlsaCtl.getCardName(cards[0]);
        assertTrue(name != null && !name.isEmpty());
        String longName = AlsaCtl.getCardLongName(cards[0]);
        assertTrue(longName != null && !longName.isEmpty());
        assertNotEquals(name, longName);
        logger.log(Level.DEBUG, "card name: " + name);
        logger.log(Level.DEBUG, "card long name: " + longName);
    }
}
