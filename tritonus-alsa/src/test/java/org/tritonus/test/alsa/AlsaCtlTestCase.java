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

import org.junit.jupiter.api.Test;
import org.tritonus.lowlevel.alsa.AlsaCtl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class AlsaCtlTestCase {

    private static final boolean DEBUG = false;
    private static final String CARD_NAME_FOR_INDEX_TEST = "LIFE";

    @Test
    public void testGetCards() {
        int[] anCards = AlsaCtl.getCards();
        assertNotNull(anCards);
        assertEquals(1, anCards.length);
        assertTrue(anCards[0] >= 0);
    }

    @Test
    public void testLoadCards() {
        int[] anCards = AlsaCtl.getCards();
        for (int anCard : anCards) {
            int nError = AlsaCtl.loadCard(anCard);
            assertTrue(nError >= 0);
        }
    }

    @Test
    public void testGetIndex() {
        int nIndex = AlsaCtl.getCardIndex(CARD_NAME_FOR_INDEX_TEST);
        if (DEBUG) {
            System.out.println("card index: " + nIndex);
        }
        assertTrue(nIndex >= 0);
        int[] anCards = AlsaCtl.getCards();
        if (DEBUG) {
            System.out.println("card index: " + anCards[0]);
        }
        assertEquals(nIndex, anCards[0]);
    }

    @Test
    public void testGetNames() {
        int[] anCards = AlsaCtl.getCards();
        String strName = AlsaCtl.getCardName(anCards[0]);
        assertTrue(strName != null && !strName.isEmpty());
        String strLongName = AlsaCtl.getCardLongName(anCards[0]);
        assertTrue(strLongName != null && !strLongName.isEmpty());
        assertNotEquals(strName, strLongName);
        if (DEBUG) {
            System.out.println("card name: " + strName);
            System.out.println("card long name: " + strLongName);
        }
    }
}


