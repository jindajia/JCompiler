package edu.ufl.cise.plpfa22;

import java.util.HashMap;
import java.util.Map;

import edu.ufl.cise.plpfa22.ast.Declaration;
import edu.ufl.cise.plpfa22.ast.ProcDec;

public class ProcedureSystem {
    public static record ProcedureInfo(String fieldName, Declaration parentDec){
    }
    Map<Declaration, ProcedureInfo> dataMap;
    public ProcedureSystem(){
        dataMap = new HashMap<>();
    }
    public void addProcedureInfo(Declaration dec, ProcedureInfo procInfo){
        dataMap.put(dec, procInfo);
    }
    public ProcedureInfo getProcedureInfo(Declaration dec) {
        return dataMap.get(dec);
    }
}
