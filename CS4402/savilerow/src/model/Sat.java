package savilerow;
/*

    Savile Row http://savilerow.cs.st-andrews.ac.uk/
    Copyright (C) 2014-2020 Patrick Spracklen and Peter Nightingale
    
    This file is part of Savile Row.
    
    Savile Row is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.
    
    Savile Row is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.
    
    You should have received a copy of the GNU General Public License
    along with Savile Row.  If not, see <http://www.gnu.org/licenses/>.

*/

import java.util.*;
import java.io.*;

import gnu.trove.map.hash.*;

public class Sat
{
    private long variableNumber=1;
    
    private long numClauses=0;
    
    //Maps the SAT variables (direct only) to SR variable/value.
    private TLongObjectHashMap<NumberMap> dimacsMapping;
    
    // Just enough info to decode order encoding, for SR vars that have only the order encoding. 
    public TLongObjectHashMap<NumberMap> orderMappingMin;
    public TLongObjectHashMap<NumberMap> orderMappingMid;
    public TLongObjectHashMap<NumberMap> orderMappingMax;
    
    //Maps Variable objects to their respective values in CNF form
    private TObjectLongHashMap<NumberMap> directVarMapping;
    
    private TObjectLongHashMap<NumberMap> orderVarMapping;
    
    private SymbolTable global_symbols;
    
    private FileOutputStream fw;
    private BufferedWriter outstream;
    
    private long trueVar;   //  A SAT variable that is true in all solutions.
    // trueVar or -trueVar is returned when a SAT variable is requested that 
    // does not exist. 
    
    private long top=1000000000;   //  A value used as a weight in Partial MaxSAT. 
    
    public Sat(SymbolTable _global_symbols)
    {
        dimacsMapping=new TLongObjectHashMap<NumberMap>();
        
        directVarMapping=new TObjectLongHashMap<NumberMap>();
        orderVarMapping=new TObjectLongHashMap<NumberMap>();
        
        orderMappingMin=new TLongObjectHashMap<NumberMap>();
        orderMappingMid=new TLongObjectHashMap<NumberMap>();
        orderMappingMax=new TLongObjectHashMap<NumberMap>();
        global_symbols=_global_symbols;
        
        assert directVarMapping.getNoEntryValue()==0L;
        assert orderVarMapping.getNoEntryValue()==0L;
        
        try {
            fw=new FileOutputStream(CmdFlags.satfile);
            outstream = new BufferedWriter(new OutputStreamWriter(fw));
            
            // Write 100 spaces at start of file, to leave space for "p cnf" line.
            for(int i=0; i<10; i++) {
                outstream.write("          ");
            }
            outstream.newLine();
            // Make the true variable. 
            trueVar=getNextVariableNumber();
            // Do not add to mappings.
            // Make trueVar true.
            addClause(trueVar);
        }
        catch ( IOException e ) {
            CmdFlags.errorExit("Failed to open or write to SAT output file.");
        }
    }
    
    
    private long variableNumberBak;
    private long numClausesBak;
    private long filesizeBak;
    
    //  Save and restore state, for mining mode.
    //  Must call finaliseOutput before either of these. 
    public void BTMark() throws IOException {
        variableNumberBak=variableNumber;
        numClausesBak=numClauses;
        
        //  Store file size so it can be truncated.
        RandomAccessFile f = new RandomAccessFile(CmdFlags.satfile, "rws");
        filesizeBak = f.length();
        f.close();
    }
    public void BTRestore() throws IOException {
        variableNumber=variableNumberBak;
        numClauses=numClausesBak;
        
        // Truncate the file.
        {
            RandomAccessFile f = new RandomAccessFile(CmdFlags.satfile, "rws");
            f.setLength(filesizeBak);
            f.close();
        }
        reopenFile();
        
        finaliseOutput();
    }
    
    //  Reopen for append after finaliseOutput
    public void reopenFile() throws IOException {
        fw=new FileOutputStream(CmdFlags.satfile, true);   ///  true for append.
        outstream = new BufferedWriter(new OutputStreamWriter(fw));
    }
    
    //Returns the next available SAT variable number.
    private long getNextVariableNumber() {
        return variableNumber++;
    }
    
    public NumberMap getDimacsMapping(long assign) {
        return dimacsMapping.get(assign);
    }
    
    // Create variable for the fact  variableName = value
    private long createSatVariableDirect(String variableName, long value) {
        NumberMap map=new NumberMap(value, variableName);
        long satvar=getNextVariableNumber();
        directVarMapping.put(map, satvar);
        
        dimacsMapping.put(satvar, map);
        
        return satvar;
    }
    
    // Create variable for the fact  variableName <= value
    private long createSatVariableOrder(String variableName, long value) {
        NumberMap map=new NumberMap(value, variableName);
        long satvar=getNextVariableNumber();
        orderVarMapping.put(map, satvar);
        return satvar;
    }
    
    public long getOrderVariable(String variableName, long value) {
        long l=orderVarMapping.get(new NumberMap(value, variableName));
        if(l==orderVarMapping.getNoEntryValue()) {
            Intpair bnds=global_symbols.getDomain(variableName).getBounds();
            if(value<bnds.lower) {
                return -trueVar;  // false.
            }
            if(value>=bnds.upper) {
                return trueVar;   // true.
            }
            
            ArrayList<Intpair> intervals=global_symbols.getDomain(variableName).getIntervalSet();
            for(int i=0; i<intervals.size()-1; i++) {
                Intpair interval1=intervals.get(i);
                Intpair interval2=intervals.get(i+1);
                // Test if the value is between the two intervals. 
                
                if(value>interval1.upper && value<interval2.lower) {
                    long l2=orderVarMapping.get(new NumberMap(interval1.upper, variableName));
                    assert l2!=orderVarMapping.getNoEntryValue();
                    return l2;
                }
            }
            assert false;
            return 1;
        }
        else {
            return l;
        }
    }
    
    public long getDirectVariable(String variableName, long value) {
        long l=directVarMapping.get(new NumberMap(value, variableName));
        if(l==directVarMapping.getNoEntryValue()) {
            return -trueVar;  // false.
        }
        else {
            return l;
        }
    }
    
    public long getTrue() {
        return trueVar;
    }
    
    // Create a new SAT variable used as an auxiliary when encoding a 
    // constraint. 
    // This is represented in the directVarMapping table using the number of the
    // variable as its name, so literals are available for var=1 and var=0
    
    // Should not have any identifiers with the number of the sat variable as their name. Removing from direct mapping table. 
    public long createAuxSATVariable() {
        return getNextVariableNumber();
    }
    
    public ASTNode createAuxSATVariableAST() {
        long satvar=createAuxSATVariable();
        return new SATLiteral(satvar, global_symbols.m);
    }
    
    //  Generate both the direct and order encoding variables, and the channelling clauses
    //  for each of the Savile Row variables.
    public void generateVariableEncoding(HashSet<String> varInConstraints) throws IOException
    {
        categoryentry catentry=global_symbols.getCategoryFirst();
        while (catentry!=null)
        {
            if(catentry.cat == ASTNode.Decision || catentry.cat == ASTNode.Auxiliary)
            {
                String ct = global_symbols.represents_ct.get(catentry.name);
                if (ct == null) {
                    ct = "";
                }
                addComment("Encoding variable: "+catentry.name+" with domain: "+global_symbols.getDomain(catentry.name)+" (representing constraint "+ct+")");
                
                ASTNode domain=global_symbols.getDomain(catentry.name);
                
                ArrayList<Intpair> intervalset=domain.getIntervalSet();
                
                // Split on number of values. 2 is special cased. 
                long numvals=Intpair.numValues(intervalset);
                
                if(numvals<=0) {
                    //  Ensure no solutions.
                    addClause(-getTrue());
                }
                else if(numvals==1) {
                    generateVariableEncoding1Val(catentry.name);
                }
                else if(numvals==2) {
                    generateVariableEncoding2Vals(catentry.name, !varInConstraints.contains(catentry.name));
                }
                else {
                    if(global_symbols.isDirectSAT(catentry.name)) {
                        if(global_symbols.isOrderSAT(catentry.name)) {
                            //  Need direct encoding and order encoding.
                            generateVariableEncodingInteger(catentry.name);
                        }
                        else {
                            //  Direct encoding, no order encoding.
                            generateVariableEncodingIntegerNoOrder(catentry.name);
                        }
                    }
                    else {
                        generateVariableEncodingIntegerNoDirect(catentry.name);
                    }
                }
            }
            catentry=catentry.next;
        }
    }
    
    // For all optimisation levels to work we need an encoding of a variable
    // with 1 value. 
    public void generateVariableEncoding1Val(String name) throws IOException
    {
        ASTNode domain=global_symbols.getDomain(name);
        Intpair bnds=domain.getBounds();
        assert bnds.upper==bnds.lower;
        
        long val1=bnds.lower;
        
        long satvar=getNextVariableNumber();
        addComment("Var represented with SAT variable "+satvar);
        
        NumberMap val1map=new NumberMap(val1, name);
        
        directVarMapping.put(val1map, getTrue());
        // Make sure the variable is always true
        addClause(satvar);
        
        dimacsMapping.put(satvar, val1map);
    }
    
    // noConstraints is true if this SR variable is mentioned in no constraints. 
    public void generateVariableEncoding2Vals(String name, boolean noConstraints) throws IOException
    {
        // Encode an SR variable with 2 values as a single SAT variable. 
        
        ASTNode domain=global_symbols.getDomain(name);
        Intpair bnds=domain.getBounds();
        
        long val1=bnds.lower;
        long val2=bnds.upper;
        
        // Make the SAT variable without using createSatVariableDirect
        long satvar=getNextVariableNumber();
        addComment("Var represented with SAT variable "+satvar);
        
        NumberMap val1map=new NumberMap(val1, name);
        NumberMap val2map=new NumberMap(val2, name);
        
        directVarMapping.put(val2map, satvar);
        directVarMapping.put(val1map, -satvar);
        
        //  A two-valued variable is always <= val2
        orderVarMapping.put(val2map, trueVar);
        // The variable is <=val1 if it is not =val2. 
        orderVarMapping.put(val1map, -satvar);
        
        // For translation back from SAT solution.
        dimacsMapping.put(satvar, val2map);
        dimacsMapping.put(-satvar, val1map);
        
        if(noConstraints) {
            addClause(satvar, -satvar);   // Make sure this sat variable is mentioned in a clause, otherwise the SAT solver will rudely ignore it.
        }
    }
    
    public void generateVariableEncodingInteger(String name) throws IOException 
    {
        ASTNode domain=global_symbols.getDomain(name);
        ArrayList<Intpair> intervalset=domain.getIntervalSet();
        
        // Need to keep track of the previous order variable. Initially false. (x <= lowerbound-1 is false) 
        long prevordervar=-trueVar;
        
        for(int intervalidx=0; intervalidx<intervalset.size(); intervalidx++) {
            Intpair bnds=intervalset.get(intervalidx);
            
            for (long i=bnds.lower; i<=bnds.upper; i++)
            {
                boolean firstValue= i==bnds.lower && intervalidx==0;
                boolean lastValue= i==bnds.upper && intervalidx==intervalset.size()-1;
                
                long directvar;
                long ordervar=trueVar;
                
                if(!lastValue) {
                    ordervar=createSatVariableOrder(name, i);
                    
                    // Ladder clause   e.g. [x<=5] -> [x<=6]
                    if( prevordervar != -trueVar ) {
                        addClause(-prevordervar, ordervar);
                    }
                }
                
                if(lastValue) {
                    directvar=-prevordervar; // last direct variable is the negation of second-last order variable.
                    
                    // Because no SAT variable is created, need to add some mappings. 
                    NumberMap directmap=new NumberMap(i, name);
                    directVarMapping.put(directmap, directvar);
                    dimacsMapping.put(directvar, directmap);
                }
                else if(firstValue) {
                    directvar=ordervar;
                    
                    // Because no SAT variable is created, need to add some mappings. 
                    NumberMap directmap=new NumberMap(i, name);
                    directVarMapping.put(directmap, directvar);
                    dimacsMapping.put(directvar, directmap);
                }
                else {
                    directvar=createSatVariableDirect(name, i);
                }
                
                if(!firstValue && !lastValue) {
                    // Channelling clauses -- connect the direct and order variable.
                    // e.g. -[x<=5] /\ [x<=4] -> [x=4]
                    addClause(prevordervar, -ordervar, directvar);  //  This clause is optional with the long ALO below. 
                    
                    // e.g. [x=5] -> [x<=5]
                    addClause(-directvar, ordervar);
                    
                    // e.g. [x=5] -> -[x<=4]
                    addClause(-directvar, -prevordervar);
                }
                
                prevordervar=ordervar;  // For next iteration.
            }
        }
        
        // Optional extra: ALO clause
        ArrayList<Long> alo=new ArrayList<Long>();
        for(int intervalidx=0; intervalidx<intervalset.size(); intervalidx++) {
            Intpair bnds=intervalset.get(intervalidx);
            for(long i=bnds.lower; i<=bnds.upper; i++)
            {
                alo.add(getDirectVariable(name, i));
            }
        }
        addClause(alo);
        
    }
    
    public void generateVariableEncodingIntegerNoOrder(String name) throws IOException 
    {
        ASTNode domain=global_symbols.getDomain(name);
        ArrayList<Intpair> intervalset=domain.getIntervalSet();
        
        ArrayList<ASTNode> amo=new ArrayList<ASTNode>();
        
        for(int intervalidx=0; intervalidx<intervalset.size(); intervalidx++) {
            Intpair bnds=intervalset.get(intervalidx);
            
            for (long i=bnds.lower; i<=bnds.upper; i++)
            {
                long dvar=createSatVariableDirect(name, i);
                amo.add(new SATLiteral(dvar, global_symbols.m));
            }
        }
        
        //  EO encoding.
        productEncodingAMO(amo, true);
    }
    
    public void generateVariableEncodingIntegerNoDirect(String name) throws IOException 
    {
        ASTNode domain=global_symbols.getDomain(name);
        ArrayList<Intpair> intervalset=domain.getIntervalSet();
        
        // Need to keep track of the previous order variable. Initially false. (x <= lowerbound-1 is false) 
        long prevordervar=-trueVar;
        
        for(int intervalidx=0; intervalidx<intervalset.size(); intervalidx++) {
            Intpair bnds=intervalset.get(intervalidx);
            
            for (long i=bnds.lower; i<=bnds.upper; i++)
            {
                boolean lastValue= i==bnds.upper && intervalidx==intervalset.size()-1;
                
                NumberMap n=new NumberMap(i, name);
                
                if(!lastValue) {
                    long ordervar=createSatVariableOrder(name, i);
                    
                    // Ladder clause   e.g. [x<=5] -> [x<=6]
                    if( prevordervar != -trueVar ) {
                        addClause( -prevordervar, ordervar);
                        
                        //  Populate tables for mapping back to SR var/val.
                        orderMappingMid.put(ordervar, n);
                    }
                    else {
                        orderMappingMin.put(ordervar, n);
                    }
                    
                    prevordervar=ordervar;  // For next iteration.
                }
                else {
                    orderMappingMax.put(-prevordervar, n);  //  If topmost order var is false, that indicates the max value of the domain is assigned. 
                }
            }
        }
    }
    
    public void addClause(long lit1) throws IOException
    {
        if(CmdFlags.getMaxsattrans()) {
            outstream.write(String.valueOf(top));
            outstream.write(" ");
        }
        
        if(lit1!=-trueVar) {
            outstream.write(String.valueOf(lit1));
            outstream.write(" ");
        }
        
        outstream.write("0");
        outstream.newLine();
        numClauses++;
        
        if(CmdFlags.getCNFLimit()!=0) {
            if(numClauses>CmdFlags.getCNFLimit()) {
                CmdFlags.println("ERROR: Reached CNF clause limit.");
                throw new IOException();
            }
        }
    }
    
    public void addClause(long lit1, long lit2) throws IOException
    {
        if(lit1==trueVar || lit2==trueVar) return;
        
        if(CmdFlags.getMaxsattrans()) {
            outstream.write(String.valueOf(top));
            outstream.write(" ");
        }
        
        if(lit1!=-trueVar) {
            outstream.write(String.valueOf(lit1));
            outstream.write(" ");
        }
        if(lit2!=-trueVar) {
            outstream.write(String.valueOf(lit2));
            outstream.write(" ");
        }
        outstream.write("0");
        outstream.newLine();
        numClauses++;
        
        if(CmdFlags.getCNFLimit()!=0) {
            if(numClauses>CmdFlags.getCNFLimit()) {
                CmdFlags.println("ERROR: Reached CNF clause limit.");
                throw new IOException();
            }
        }
    }
    
    public void addClause(long lit1, long lit2, long lit3) throws IOException
    {
        if(lit1==trueVar || lit2==trueVar || lit3==trueVar) return;
        
        if(CmdFlags.getMaxsattrans()) {
            outstream.write(String.valueOf(top));
            outstream.write(" ");
        }
        
        if(lit1!=-trueVar) {
            outstream.write(String.valueOf(lit1));
            outstream.write(" ");
        }
        if(lit2!=-trueVar) {
            outstream.write(String.valueOf(lit2));
            outstream.write(" ");
        }
        if(lit3!=-trueVar) {
            outstream.write(String.valueOf(lit3));
            outstream.write(" ");
        }
        outstream.write("0");
        outstream.newLine();
        numClauses++;
        
        if(CmdFlags.getCNFLimit()!=0) {
            if(numClauses>CmdFlags.getCNFLimit()) {
                CmdFlags.println("ERROR: Reached CNF clause limit.");
                throw new IOException();
            }
        }
    }
    
    public void addClause(ArrayList<Long> literals) throws IOException
    {
        for(int i=0; i<literals.size(); i++) {
            if(literals.get(i)==trueVar) {
                return;
            }
        }
        
        if(CmdFlags.getMaxsattrans()) {
            outstream.write(String.valueOf(top));
            outstream.write(" ");
        }
        
        for(int i=0; i<literals.size(); i++) {
            if(literals.get(i)!=-trueVar) {
                outstream.write(String.valueOf(literals.get(i)));
                outstream.write(" ");
            }
        }
        outstream.write("0");
        outstream.newLine();
        
        numClauses++;
        
        if(CmdFlags.getCNFLimit()!=0) {
            if(numClauses>CmdFlags.getCNFLimit()) {
                CmdFlags.println("ERROR: Reached CNF clause limit.");
                throw new IOException();
            }
        }
    }
    
    public void addClauseReified(ArrayList<Long> literals, long auxVar) throws IOException
    {
        if(CmdFlags.getMaxsattrans()) {
            outstream.write(String.valueOf(top));
            outstream.write(" ");
        }
        
        // auxVar -> literals
        outstream.write(String.valueOf(-auxVar));
        outstream.write(" ");
        
        for(int i=0; i<literals.size(); i++) {
            outstream.write(String.valueOf(literals.get(i)));
            outstream.write(" ");
        }
        outstream.write("0");
        outstream.newLine();
        numClauses++;
        
        // For each literal, literal -> auxVar.
        for(int i=0; i<literals.size(); i++) {
            if(CmdFlags.getMaxsattrans()) {
                outstream.write(String.valueOf(top));
                outstream.write(" ");
            }
            
            outstream.write(String.valueOf(-literals.get(i)));
            outstream.write(" ");
            outstream.write(String.valueOf(auxVar));
            outstream.write(" 0");
            outstream.newLine();
            numClauses++;
        }
        
        if(CmdFlags.getCNFLimit()!=0) {
            if(numClauses>CmdFlags.getCNFLimit()) {
                CmdFlags.println("ERROR: Reached CNF clause limit.");
                throw new IOException();
            }
        }
    }
    
    public void addComment(String comment) throws IOException
    {
        outstream.write("c ");
        outstream.write(comment);
        outstream.newLine();
    }
    
    public void addSoftClause(long lit1) throws IOException {
        assert CmdFlags.getMaxsattrans();
        addSoftClause(lit1, 1);
    }
    
    public void addSoftClause(long lit1, long weight) throws IOException {
        assert CmdFlags.getMaxsattrans();
        outstream.write(String.valueOf(weight));
        outstream.write(" ");
        outstream.write(String.valueOf(lit1));
        outstream.write(" 0");
        outstream.newLine();
        numClauses++;
        
        if(CmdFlags.getCNFLimit()!=0) {
            if(numClauses>CmdFlags.getCNFLimit()) {
                CmdFlags.println("ERROR: Reached CNF clause limit.");
                throw new IOException();
            }
        }
    }
    
    public void finaliseOutput() throws IOException
    {
        outstream.flush();
        fw.getFD().sync();
        outstream.close();
        RandomAccessFile f=new RandomAccessFile(CmdFlags.satfile, "rws");  //  rws to make sure everything is sync'd.
        f.seek(0);
        byte[] pcnf;
        if(CmdFlags.getMaxsattrans()) {
            pcnf=("p wcnf "+(variableNumber-1)+" "+numClauses+" "+top).getBytes();
        }
        else {
            pcnf=("p cnf "+(variableNumber-1)+" "+numClauses).getBytes();
        }
        f.write(pcnf);
        f.write(("          ").getBytes());  //  Write some spaces in case there was a p cnf line already that was longer.
        f.close();
    }
    
    ////////////////////////////////////////////////////////////////////////////
    //
    //   Methods that change the SAT instance after finalise.
    //   These just re-open the file, pass through the add-clause call then 
    //   finalise again.
    
    public void addClauseAfterFinalise(ArrayList<Long> clause) throws IOException
    {
        fw=new FileOutputStream(CmdFlags.satfile, true);   ///  true for append.
        outstream = new BufferedWriter(new OutputStreamWriter(fw));
        addClause(clause);
        finaliseOutput();
    }
    
    public void addClauseAfterFinalise(long lit1) throws IOException {
        fw=new FileOutputStream(CmdFlags.satfile, true);   ///  true for append.
        outstream = new BufferedWriter(new OutputStreamWriter(fw));
        addClause(lit1);
        finaliseOutput();
    }
    
    public void removeFinalClause() throws IOException {
        {
            RandomAccessFile f = new RandomAccessFile(CmdFlags.satfile, "rws");
            long length = f.length() - 1;
            byte b;
            do {
              length -= 1;
              f.seek(length);
              b = f.readByte();
            } while(b != 10);
            f.setLength(length+1);
            f.close();
        }
        numClauses--;
        
        fw=new FileOutputStream(CmdFlags.satfile, true);   ///  true for append.
        outstream = new BufferedWriter(new OutputStreamWriter(fw));
        
        finaliseOutput();
    }
    
    ////////////////////////////////////////////////////////////////////////////
    //
    //     Methods for SAT encodings, direct & support
    
    //   Direct encoding of unary constraints
    public void unaryDirectEncoding(ASTNode constraint, ASTNode var) throws IOException
    {
        ArrayList<Intpair> domain1=var.getIntervalSetExp();
        for (Intpair pair1 : domain1)
        {
            for (long i=pair1.lower; i<=pair1.upper; i++)
            {
                if(!constraint.test(i))
                {
                    addClause(-var.directEncode(this, i));
                }
            }
        }
    }
    
    //   Direct encoding of unary constraints with aux.
    public void unaryDirectEncodingWithAuxVar(ASTNode constraint, ASTNode var, long aux) throws IOException
    {
        ArrayList<Intpair> domain1=var.getIntervalSetExp();
        for (Intpair pair1 : domain1)
        {
            for (long i=pair1.lower; i<=pair1.upper; i++)
            {
                if(!constraint.test(i))
                {
                    addClause(-aux, -var.directEncode(this, i));
                }
                else {
                    addClause(aux, -var.directEncode(this, i));
                }
            }
        }
    }
    
    public void directEncoding(ASTNode constraintNode, ASTNode node1, ASTNode node2) throws IOException
    {
        ArrayList<Intpair> domain1=node1.getIntervalSetExp();
        ArrayList<Intpair> domain2=node2.getIntervalSetExp();
        
        for (Intpair pair1 : domain1)
        {
            for (long i=pair1.lower; i<=pair1.upper; i++)
            {
                for (Intpair pair2: domain2)
                {
                    for (long j=pair2.lower; j<=pair2.upper; j++)
                    {
                        if (!constraintNode.test(i,j))
                        {
                            addClause(-node1.directEncode(this, i), -node2.directEncode(this, j));
                        }
                    }
                }
            }

        }

    }              

    public void directEncodingWithAuxVar(ASTNode constraintNode, ASTNode node1, ASTNode node2, long auxVarValue) throws IOException
    {
        ArrayList<Intpair> domain1=node1.getIntervalSetExp();
        ArrayList<Intpair> domain2=node2.getIntervalSetExp();
        
        for (Intpair pair1 : domain1)
        {
            for (long i=pair1.lower; i<=pair1.upper; i++)
            {
                for (Intpair pair2: domain2)
                {
                    for (long j=pair2.lower; j<=pair2.upper; j++)
                    {
                        if (!constraintNode.test(i,j))
                        {
                            addClause(-auxVarValue, -node1.directEncode(this, i), -node2.directEncode(this, j));
                        }
                        else {
                            addClause(auxVarValue, -node1.directEncode(this, i), -node2.directEncode(this, j));
                        }
                    }
                }
            }

        }
    }
    
    ////////////////////////////////////////////////////////////////////////////
    //  Methods for ternary constraints such as product and division.
    
    public void ternaryEncoding(ASTNode con, ASTNode auxVar) throws IOException {
        if (auxVar instanceof NumberConstant) {
            supportEncodingBinary(con, con.getChild(0), con.getChild(1), auxVar.getValue());
        }
        else {
            ternaryFunctionalEncoding(con, con.getChild(0), con.getChild(1), auxVar);
            
            // Alternative, larger encoding:
            // ternaryDirectEncoding(con, con.getChild(0), con.getChild(1), auxVar);
        }
    }
    
    /*public void ternaryDirectEncoding(ASTNode con, ASTNode node1, ASTNode node2, ASTNode node3) throws IOException
    {
        ArrayList<Intpair> dom1=node1.getIntervalSetExp();
        ArrayList<Intpair> dom2=node2.getIntervalSetExp();
        ArrayList<Intpair> dom3=node3.getIntervalSetExp();
        
        for(Intpair pair1 : dom1) {
            for(long i=pair1.lower; i<=pair1.upper; i++) {
                for(Intpair pair2: dom2) {
                    for (long j=pair2.lower; j<=pair2.upper; j++) {
                        for(Intpair pair3: dom3) {
                            for(long k=pair3.lower; k<=pair3.upper; k++) {
                                if(!con.test(i, j, k)) {
                                    addClause(-node1.directEncode(this, i), -node2.directEncode(this, j), -node3.directEncode(this, k));
                                }
                            }
                        }
                    }
                }
            }
        }
    }*/
    
    public void ternaryFunctionalEncoding(ASTNode con, ASTNode node1, ASTNode node2, ASTNode node3) throws IOException
    {
        ArrayList<Intpair> dom1=node1.getIntervalSetExp();
        ArrayList<Intpair> dom2=node2.getIntervalSetExp();
        
        for(Intpair pair1 : dom1) {
            for(long i=pair1.lower; i<=pair1.upper; i++) {
                for(Intpair pair2: dom2) {
                    for (long j=pair2.lower; j<=pair2.upper; j++) {
                        long k=con.func(i,j);
                        addClause(-node1.directEncode(this, i), -node2.directEncode(this, j), node3.directEncode(this, k));
                    }
                }
            }
        }
    }
    
    ///////////////////////////////////////////////////////////////////////////
    //  Support encoding for binary and reified binary constraints.
    
    public void supportEncodingBinary(ASTNode constraint, ASTNode node1, ASTNode node2) throws IOException
    {
        supportEncodingBinaryGenerateClauses(constraint, node1, node2, false, false, 0);      
        supportEncodingBinaryGenerateClauses(constraint, node2, node1, true, false, 0);
    }
    
    //  This one calls the three- argument constraint.test function, using auxVal as the final argument. 
    public void supportEncodingBinary(ASTNode constraint, ASTNode node1, ASTNode node2, long auxVal) throws IOException
    {
        supportEncodingBinaryGenerateClauses(constraint, node1, node2, false, true, auxVal);
        supportEncodingBinaryGenerateClauses(constraint, node2, node1, true, true, auxVal);
    }
    
    private void supportEncodingBinaryGenerateClauses(ASTNode constraint, ASTNode node1, ASTNode node2, boolean reverse, boolean threeargs, long thirdarg) throws IOException
    {
        // Reverse indicates that the arguments should be swapped for constraint.test(....)
        ArrayList<Intpair> domain1=node1.getIntervalSetExp();
        ArrayList<Intpair> domain2=node2.getIntervalSetExp();
        
        for(Intpair pair1 : domain1)
        {
            for (long i=pair1.lower; i<=pair1.upper; i++)
            {
                ArrayList<Long> clause=new ArrayList<Long>();
                clause.add(-node1.directEncode(this,i));
                
                for (Intpair pair2: domain2)
                {
                    for (long j=pair2.lower; j<=pair2.upper; j++)
                    {
                        if( (!threeargs && ((! reverse && constraint.test(i,j)) || (reverse && constraint.test(j,i)))) ||
                            (threeargs && ((!reverse && constraint.test(i,j,thirdarg)) || (reverse && constraint.test(j,i,thirdarg)))) ) {
                            clause.add(node2.directEncode(this,j));
                        }
                    }
                }
                addClause(clause);
            }
        }
    }
    
    public void supportEncodingBinaryWithAuxVar(ASTNode constraint, ASTNode node1, ASTNode node2, long auxVarValue) throws IOException
    {
        //If either of the nodes in this binary constraint are a Constant value resort to using the Direct Encoding
        if (node1.getCategory()==ASTNode.Constant || node2.getCategory()==ASTNode.Constant){
           // System.out.println("Resorting to the Direct Encoding for this constraint : " + constraint.toString());
            directEncodingWithAuxVar(constraint,node1,node2,auxVarValue);
        }else {   
            supportEncodingBinaryGenerateClausesWithAuxVar(constraint, node1, node2, auxVarValue, false);      
            supportEncodingBinaryGenerateClausesWithAuxVar(constraint, node2, node1, auxVarValue, true);
        }
    }
    
    private void supportEncodingBinaryGenerateClausesWithAuxVar(ASTNode constraint, ASTNode node1, ASTNode node2, long auxVarValue, boolean reverse) throws IOException
    {
        // Reverse indicates that the arguments should be swapped for constraint.test(....)
        ArrayList<Intpair> domain1=node1.getIntervalSetExp();
        ArrayList<Intpair> domain2=node2.getIntervalSetExp();
        
        for (Intpair pair1 : domain1)
        {
            for (long i=pair1.lower; i<=pair1.upper; i++)
            {
                ArrayList<Long> supportClause=new ArrayList<Long>();
                supportClause.add(auxVarValue);
                supportClause.add(-node1.directEncode(this,i));
                
                ArrayList<Long> conflictClause=new ArrayList<Long>();
                
                conflictClause.add(-auxVarValue);
                conflictClause.add(-node1.directEncode(this, i));
                
                for (Intpair pair2: domain2)
                {
                    for (long j=pair2.lower; j<=pair2.upper; j++)
                    {
                        if( (!reverse && constraint.test(i,j)) || (reverse && constraint.test(j,i)) )
                        {
                            conflictClause.add(node2.directEncode(this,j));
                        }
                        else {
                            supportClause.add(node2.directEncode(this,j));
                        }
                    }
                }
                addClause(supportClause);
                addClause(conflictClause);
            }
        }
    }
    
    /////////////////////////////////////////////////////////////////////////
    //
    //   Ladder encoding for AMO and EO
    
    public void ladderEncodingAMO(ArrayList<ASTNode> ch, boolean exactlyOne) throws IOException {
        if(!exactlyOne && !CmdFlags.getAuxNonFunctional()) {
            //  Aux vars must be functionally defined, and this is an AMO. 
            //  Make it an EO. 
            ch.add(createAuxSATVariableAST());
            exactlyOne=true;
        }
        ladderEncodingAMOinner(ch, exactlyOne);
    }
    
    public void ladderEncodingAMOinner(ArrayList<ASTNode> ch, boolean exactlyOne) throws IOException {
        ArrayList<ASTNode> ladder=new ArrayList<ASTNode>(ch.size()-1);
        
        ladder.add(ch.get(0));
        for(int i=1; i<ch.size()-1; i++) {
            ladder.add(createAuxSATVariableAST());
        }
        
        //  ladder[i] means x<=i
        //  Ladder consistency,  ladder[i]->ladder[i+1]
        for(int i=0; i<ladder.size()-1; i++) {
            addClause(ladder.get(i).directEncode(this, 0), ladder.get(i+1).directEncode(this, 1));
        }
        
        // Connect ch variables to ladder. 
        // Skip the first. 
        for(int i=1; i<ch.size(); i++) {
            addClause(ch.get(i).directEncode(this, 0), ladder.get(i-1).directEncode(this, 0));
            if(i<ch.size()-1) {
                addClause(ch.get(i).directEncode(this, 0), ladder.get(i).directEncode(this, 1));
            }
        }
        
        if(exactlyOne) {
            alo(ch);
        }
        
        // Can get rid of first ladder variable, equal to first variable in ch. 
        // Functional aux variables not dealt with here. 
    }
    
    private void alo(ArrayList<ASTNode> ch) throws IOException {
        ArrayList<Long> clause=new ArrayList<Long>(ch.size());
        for(int j=0; j<ch.size(); j++) {
            clause.add(ch.get(j).directEncode(this, 1));
        }
        addClause(clause);
    }
    
    /////////////////////////////////////////////////////////////////////////
    //
    //   Product encoding for AMO and EO
    
    public void productEncodingAMO(ArrayList<ASTNode> ch, boolean exactlyOne) throws IOException {
        if(!exactlyOne && !CmdFlags.getAuxNonFunctional()) {
            //  Aux vars must be functionally defined, and this is an AMO. 
            //  Make it an EO. 
            ch.add(createAuxSATVariableAST());
            exactlyOne=true;
        }
        productEncodingAMOinner(ch, exactlyOne);
    }
    
    public void productEncodingAMOinner(ArrayList<ASTNode> ch, boolean exactlyOne) throws IOException {
        int n=ch.size();
        
        //  If exactly-one, then a single clause is added to ensure at least one of ch. 
        if(exactlyOne) {
            alo(ch);
        }
        
        if(n<=6) {
            //  AMO of 6 or fewer variables is smaller (in # binary clauses) with binomial encoding. 
            AMOBinomial(ch, false);
        }
        else {
            int p=(int)Math.ceil(Math.sqrt(n));
            int q=(int)Math.ceil(((double)n)/p);
            
            // Make u and v variables
            
            ArrayList<ASTNode> u=new ArrayList<ASTNode>(p);
            ArrayList<ASTNode> v=new ArrayList<ASTNode>(q);
            
            for(int i=0; i<p; i++) {
                u.add(createAuxSATVariableAST());
            }
            for(int i=0; i<q; i++) {
                v.add(createAuxSATVariableAST());
            }
            
            for(int i=0; i<n; i++) {
                addClause(ch.get(i).directEncode(this, 0), u.get(i%p).directEncode(this, 1));
                addClause(ch.get(i).directEncode(this, 0), v.get(i/p).directEncode(this, 1));
            }
            
            //  Recurse for u and v. 
            productEncodingAMOinner(u, false);
            productEncodingAMOinner(v, false);
        }
    }
    
    ////////////////////////////////////////////////////////////////////////////
    //
    //  Commander encoding for AMO and EO. 
    
    public static int commanderPartSize=3;
    
    public void commanderEncodingAMO(ArrayList<ASTNode> ch, boolean exactlyOne) throws IOException {
        // ch must be boolean
        if(ch.size()<=commanderPartSize) {
            //  Base case
            AMOBinomial(ch, exactlyOne);
        }
        else {
            // Split into pieces of size at most commanderPartSize
            // Each has a commander variable in commander
            ArrayList<ASTNode> commander=new ArrayList<ASTNode>();
            
            for(int i=0; i<ch.size(); i=i+commanderPartSize) {
                ArrayList<ASTNode> ch2=new ArrayList<ASTNode>(commanderPartSize);
                for(int j=i; j<i+commanderPartSize && j<ch.size(); j++) {
                    ch2.add(ch.get(j));
                }
                if(ch2.size()>1) {
                    //  If the cell has size more than 1, then do the usual commander encoding
                    AMOBinomial(ch2, false);  // Ensure at most one within this cell
                    
                    ASTNode comm=createAuxSATVariableAST();
                    
                    commander.add(comm);
                    
                    for(int j=0; j<ch2.size(); j++) {
                        addClause(ch2.get(j).directEncode(this, 0), comm.directEncode(this, 1));
                    }
                    
                    if(exactlyOne || !CmdFlags.getAuxNonFunctional()) {
                        // comm -> any one of ch2.
                        ArrayList<Long> clause=new ArrayList<Long>(ch2.size()+1);
                        clause.add(comm.directEncode(this,0));
                        for(int j=0; j<ch2.size(); j++) {
                            clause.add(ch2.get(j).directEncode(this, 1));
                        }
                        addClause(clause);
                    }
                }
                else {
                    //  If the cell has size 1, just promote that 1 variable to be a commander variable. 
                    commander.add(ch2.get(0));
                }
            }
            
            // Recursively apply the commander encoding to the commander variables.
            commanderEncodingAMO(commander, exactlyOne);
        }
    }
    
    //  Binary AMO. 
    public void AMOBinomial(ArrayList<ASTNode> ch, boolean exactlyOne) throws IOException {
        for(int i=0; i<ch.size(); i++) {
            for(int j=i+1; j<ch.size(); j++) {
                addClause(ch.get(i).directEncode(this, 0), ch.get(j).directEncode(this, 0));
            }
        }
        if(exactlyOne) {
            // one of ch must be true.
            alo(ch);
        }
    }
    
    //  Eligible for commander encoding or product encoding.
    public static boolean eligibleAMO(ASTNode curnode) {
        if(CmdFlags.sat_amo_encoding==AMOEnc.TREE) {
            return false;
        }
        if(! curnode.getParent().getParent().inTopAnd()) {
            return false;  // Current version of commander can only be used for top-level constraints.
        }
        boolean commander=false;  // Eligible for commander encoding?
        ASTNode par=curnode.getParent();
        // Is curnode <= 1 or curnode=1
        if( (par instanceof LessEqual || par instanceof Equals || par instanceof ToVariable) && par.getChild(0)==curnode && par.getChild(1).isConstant() && par.getChild(1).getValue()==1) {
            commander=true;
        }
        else if(par instanceof Equals && par.getChild(1)==curnode && par.getChild(0).isConstant() && par.getChild(0).getValue()==1) {
            //  Other way around.
            commander=true;
        }
        
        // Not eligible if any child is not boolean or has a coefficient other than 1. 
        for(int i=0; i<curnode.numChildren(); i++) {
            if( (!curnode.getChild(i).isRelation()) || ((WeightedSum)curnode).getWeight(i)!=1) {
                commander=false;
            }
        }
        return commander;
    }
    
    
    public long getNumVars() {
        return variableNumber-1;
    }
    public long getNumClauses() {
        return numClauses;
    }
}
