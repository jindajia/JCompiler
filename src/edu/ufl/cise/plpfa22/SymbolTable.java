package edu.ufl.cise.plpfa22;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Random;


public class SymbolTable {

    static class SymbolEntry {
        Integer scopeID;
        Integer level;
		Object attribute;
		SymbolEntry next;
        public SymbolEntry(Integer scopeID, Integer level, Object attribute) {
			super();
            this.scopeID = scopeID;
			this.level = level;
			this.attribute = attribute;
        }
    }

    HashMap<String, LinkedList<SymbolEntry>> table;
    LinkedList<Integer> scopeStack;//use a linkedList, because of it's easier way to iterate.
    Integer level;
    HashSet<Integer> scopeIDSet;
    Random random;
    Integer globalScope;

    public SymbolTable() {
        table = new HashMap<>();
        scopeIDSet = new HashSet<>();
        scopeStack = new LinkedList<>();
        random = new Random();
        level = -1;
    }

    public Integer getLevel() {
        return level;
    }

    public Integer getNextScopeId() {
        Integer r = random.nextInt(Integer.MAX_VALUE);
        while (scopeIDSet.contains(r)) {
            r = random.nextInt(Integer.MAX_VALUE);
        }
        return r;
    }

    public void entryScope() {
        Integer scopeID = getNextScopeId();
        scopeStack.addFirst(scopeID);
        scopeIDSet.add(scopeID);
        if (level==-1) {
            globalScope = scopeID;
        }
        level++;
    }

    public void closeScope() {
        scopeStack.removeFirst();
        level--;
    }

    public boolean entryValid(SymbolEntry entry) {
        for (Integer id:scopeStack) {
            if (id==entry.scopeID) {
                return true;
            }
        }
        return false;
    }

    public SymbolEntry lookupEntry(String name) {
        LinkedList<SymbolEntry> list = table.get(name);
        if (list==null) {
            return null;
        }
        for (SymbolEntry entry:list) {
            if (entryValid(entry)) {
                return entry;
            }
        }
        return null;
    }
    
    public boolean insertAttribute(String name, Object attribute) {
        SymbolEntry preEntry = lookupEntry(name);
        if (preEntry==null || preEntry.scopeID!=scopeStack.getFirst()) {
            LinkedList<SymbolEntry> list = table.get(name);
            if (list==null) {
                list = new LinkedList<>();
            }
            list.addFirst(new SymbolEntry(scopeStack.getFirst(), level, attribute));
            table.put(name, list);
            return true;
        }
        return false;
    }

    public boolean entryProcValid(SymbolEntry entry) {
        for (Integer id:scopeStack) {
            if (id==entry.scopeID) {
                return true;
            }
        }
        return false;
    }

    public SymbolEntry lookupProcEntry(String name) {
        LinkedList<SymbolEntry> list = table.get(name);
        if (list==null) {
            return null;
        }
        for (SymbolEntry entry:list) {
            if (entryProcValid(entry)) {
                return entry;
            }
        }
        return null;
    }

    public boolean insertProcedure(String name, Object procDec) {
        SymbolEntry preEntry = lookupProcEntry(name);
        if (preEntry==null || preEntry.scopeID!=scopeStack.getFirst()) {
            LinkedList<SymbolEntry> list = table.get(name);
            if (list==null) {
                list = new LinkedList<>();
            }
            list.addLast(new SymbolEntry(scopeStack.getFirst(), level, procDec));//add to last position, because it's a procedure decalartion.
            table.put(name, list);
            return true;
        }
        return false;
    }
}
