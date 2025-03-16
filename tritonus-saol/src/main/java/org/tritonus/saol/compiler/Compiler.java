/*
 *  Copyright (c) 2002 by Matthias Pfisterer
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

package org.tritonus.saol.compiler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PushbackReader;
import java.io.Reader;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.tritonus.saol.engine.AbstractInstrument;
import org.tritonus.saol.sablecc.lexer.Lexer;
import org.tritonus.saol.sablecc.node.AGlobaldeclGlobaldecl;
import org.tritonus.saol.sablecc.node.AInstrdeclInstrdecl;
import org.tritonus.saol.sablecc.node.Start;
import org.tritonus.saol.sablecc.parser.Parser;

import static java.lang.System.getLogger;


/**
 * Compiler.
 *
 * This file is part of Tritonus: http://www.tritonus.org/
 */
public class Compiler {

    private static final Logger logger = getLogger(Compiler.class.getName());

    private static final int ACTION_DUMP_TREE = 0;
    private static final int ACTION_COMPILE_INSTRUMENTS = 1;
    private static final int ACTION_GENERATE_MP4 = 2;

    private final File saolFile;
    private final int action;

    private Map<String, Class<AbstractInstrument>> instrumentMap;

    public Compiler(File saolFile) {
        this(saolFile, ACTION_COMPILE_INSTRUMENTS);
    }

    public Compiler(File saolFile, int action) {
        this.saolFile = saolFile;
        this.action = action;
    }

    public void compile() throws Exception {
        Reader reader = new FileReader(saolFile);
        reader = new BufferedReader(reader);
        PushbackReader pbReader = new PushbackReader(reader, 1024);
        Lexer lexer = new Lexer(pbReader);
        Parser parser = new Parser(lexer);
        Start tree = parser.parse();

        switch (action) {
        case ACTION_DUMP_TREE:
            dumpTree(tree);
            break;

        case ACTION_COMPILE_INSTRUMENTS:
            logger.log(Level.TRACE, "compiling instruments...");
            instrumentMap = compileInstruments(tree);
            logger.log(Level.TRACE, "IM: " + instrumentMap);
            break;
        }
    }

    /**
     * Prints the tree of the given AST on standard output.
     */
    private static void dumpTree(Start tree) {
        PrintWalker printWalker = new PrintWalker();
        tree.apply(printWalker);
    }

    /**
     * Returns a Map: instrument names (String) -> instrument classes (Class)
     */
    private static Map<String, Class<AbstractInstrument>> compileInstruments(Start tree) {
        InstrumentTable instrumentTable = new InstrumentTable();
        UserOpcodeTable opcodeTable = new UserOpcodeTable();
        TemplateTable templateTable = new TemplateTable();
        Map<String, Class<AbstractInstrument>> instrumentMap = new HashMap<>();
        NodeSemanticsTable nodeSemanticsTable = new NodeSemanticsTable();

        // Divide the AST into sections. There is one section for global, and
        // one for each instrument, opcode or template.
        //
        TreeDivider treeDivider = new TreeDivider(instrumentTable, opcodeTable, templateTable);
        tree.apply(treeDivider);
        AGlobaldeclGlobaldecl globalNode = treeDivider.getGlobalNode();

        // Process the global section.
        //
        SAOLGlobals saolGlobals = new SAOLGlobals();
        if (globalNode != null) {
            GlobalsSearcher gsearcher = new GlobalsSearcher(saolGlobals);
            globalNode.apply(gsearcher);
        }
        logger.log(Level.DEBUG, "a-rate: " + saolGlobals.getARate());
        logger.log(Level.DEBUG, "k-rate: " + saolGlobals.getKRate());
        logger.log(Level.DEBUG, "inchannels: " + saolGlobals.getInChannels());
        logger.log(Level.DEBUG, "outchannels: " + saolGlobals.getOutChannels());
        logger.log(Level.DEBUG, "interp: " + saolGlobals.getInterP());

        VariableTable globalVariableTable = new VariableTable();

        // Semantic check on instruments.
        //
        Iterator<InstrumentEntry> instruments = instrumentTable.values().iterator();
        while (instruments.hasNext()) {
            InstrumentEntry entry = instruments.next();
            AInstrdeclInstrdecl startNode = entry.getStartNode();
            VariableTable localVariableTable = entry.getLocalVariableTable();
            InstrumentSemanticsCheck isc = new InstrumentSemanticsCheck(
                    globalVariableTable, localVariableTable, nodeSemanticsTable);
            startNode.apply(isc);
        }
        // TODO collection of variable tables, semantic checks

        // Compiling the instruments.
        //
        InstrumentCompilation ic = new InstrumentCompilation(saolGlobals, instrumentMap);
        instruments = instrumentTable.values().iterator();
        while (instruments.hasNext()) {
            InstrumentEntry entry = instruments.next();
            AInstrdeclInstrdecl node = entry.getStartNode();
            node.apply(ic);
        }

        if (logger.isLoggable(Level.DEBUG)) {
            for (String s : instrumentMap.keySet()) {
                logger.log(Level.DEBUG, s);
            }
        }
        return instrumentMap;
    }

    public Map<String, Class<AbstractInstrument>> getInstrumentMap() {
        if (action != ACTION_COMPILE_INSTRUMENTS) {
            logger.log(Level.TRACE, "I.M.: returning null");
            return null;
        }
        logger.log(Level.TRACE, "I.M.: " + instrumentMap);
        return instrumentMap;
    }

    public static void main(String[] args) {
        int action = ACTION_COMPILE_INSTRUMENTS;
        String saolFilename = args[0];
        if (args[0].equals("-d")) {
            action = ACTION_DUMP_TREE;
            saolFilename = args[1];
        }
        File saolFile = new File(saolFilename);
        Compiler compiler = new Compiler(saolFile, action);
        try {
            compiler.compile();
        } catch (Exception e) {
            logger.log(Level.ERROR, e.getMessage(), e);
        }
    }
}
