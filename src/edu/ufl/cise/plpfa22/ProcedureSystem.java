package edu.ufl.cise.plpfa22;

import java.util.HashMap;
import java.util.Map;

public class ProcedureSystem {
    public static record ProcedureInfo(String currentfieldName, String parentFieldName){
    }
    Map<String, ProcedureInfo> dataMap;
    public ProcedureSystem(){
        dataMap = new HashMap<>();
    }
    public void addProcedureInfo(String name, ProcedureInfo procInfo){
        dataMap.put(name, procInfo);
    }
    public ProcedureInfo getProcedureInfo(String name) {
        return dataMap.get(name);
    }
}
