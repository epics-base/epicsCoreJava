/*
 * License terms for this software can be found in the file LICENSE that is included with the distribution
 */
package org.epics.pvdata.copy;

import org.epics.pvdata.factory.FieldFactory;
import org.epics.pvdata.factory.PVDataFactory;
import org.epics.pvdata.pv.*;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * @author mrk
 *
 * This is mainly used by pvAccess clients.
 * Given a request string it creates a pvRequest structure that can be passed to the pvAccess create methods.
 * In turn pvAccess passes the pvRequest to a local channel provider which then passes it to pvCopy.
 */
public class CreateRequest {
    /**
     * Create CreateRequest.
     * @return new instance of CreateRequest
     */
    public static CreateRequest create()
    {
        return new CreateRequest();
    }
    /**
     * Create a request structure for the create methods in Channel.
     * See the package overview documentation for details.
     * @param request The field request. See the package overview documentation for details.
     * @return The request structure.
     * If an invalid request was given null is returned and getMessage can be called to find the reason for the failure.
     */
    public PVStructure createRequest(String request) {
        return createRequestInternal(request);
    }
    /**
     * Get the reason why the last call to createRequest returned null.
     * @return The reason why createRequest failed.
     */
    public String getMessage() {
        return message;
    }

    private static final PVDataCreate pvDataCreate = PVDataFactory.getPVDataCreate();
    private static final FieldCreate fieldCreate = FieldFactory.getFieldCreate();
    private static final Structure emptyStructure = fieldCreate.createStructure(new String[0], new Field[0]);
    private static final Pattern commaPattern = Pattern.compile("[,]");
    private static final Pattern equalPattern = Pattern.compile("[=]");
    private String message;

    private class Node {
        String name;
        List<Node> nodes = new ArrayList<Node> ();
        Node(String name) {
            this .name = name;
        }
    }

    private class OptionPair {
        String name;
        String value;
        OptionPair(String name,String value) {
            this.name = name;
            this.value = value;
        }
    }

    private List<OptionPair> optionList = new ArrayList<OptionPair>();

    private String fullFieldName = "";

    private PVStructure createRequestInternal(String request)
    {
        try {
            message = "";
            fullFieldName = "";
            optionList.clear();
            if(request!=null) request = request.replaceAll("\\s+","");
            if(request==null || request.length()<=0) {
                PVStructure pvStructure =  pvDataCreate.createPVStructure(emptyStructure);
                return pvStructure;
            }
            int offsetRecord = request.indexOf("record[");
            int offsetField = request.indexOf("field(");
            int offsetPutField = request.indexOf("putField(");
            int offsetGetField = request.indexOf("getField(");
            if(offsetRecord==-1&&offsetField==-1&&offsetPutField==-1&&offsetGetField==-1) {
                request = "field(" + request + ")";
                offsetField = request.indexOf("field(");
            }
            int numParan = 0;
            int numBrace = 0;
            int numBracket = 0;
            for(int i=0; i< request.length() ; ++i) {
                char chr = request.charAt(i);
                if(chr=='(') numParan++;
                if(chr==')') numParan--;
                if(chr=='{') numBrace++;
                if(chr=='}') numBrace--;
                if(chr=='[') numBracket++;
                if(chr==']') numBracket--;
            }
            if(numParan!=0) {
                message = "mismatched () " + numParan;
                return null;
            }
            if(numBrace!=0) {
                message = "mismatched {} " + numBrace;
                return null;
            }
            if(numBracket!=0) {
                message = "mismatched [] " + numBracket;
                return null;
            }
            List<Node> top = new ArrayList<Node>();
            try {
                if(offsetRecord>=0) {
                    fullFieldName = "record";
                    int openBracket = request.indexOf('[', offsetRecord);
                    int closeBracket = request.indexOf(']', openBracket);
                    if(closeBracket==-1) {
                        message = request.substring(offsetRecord) + "record[ does not have matching ]";
                        return null;
                    }
                    if(closeBracket-openBracket > 3) {
                        Node node = new Node("record");
                        Node optNode = createRequestOptions(
                                request.substring(openBracket+1,closeBracket));
                        node.nodes.add(optNode);
                        top.add(node);
                    }
                }
                if(offsetField>=0) {
                    fullFieldName = "field";
                    Node node = new Node("field");
                    int openParan = request.indexOf('(', offsetField);
                    int closeParan = request.indexOf(')', openParan);
                    if(closeParan == -1) {
                        message = request.substring(offsetField)
                                + " field( does not have matching )";
                        return null;
                    }
                    if(closeParan>openParan+1) {
                        String req = request.substring(openParan+1,closeParan);
                        req = createTopOption(node,req);
                        createSubNode(node,req);
                    }
                    top.add(node);
                }
                if(offsetGetField>=0) {
                    fullFieldName = "getField";
                    Node node = new Node("getField");
                    int openParan = request.indexOf('(', offsetGetField);
                    int closeParan = request.indexOf(')', openParan);
                    if(closeParan == -1) {
                        message = request.substring(offsetField)
                                + " getField( does not have matching )";
                        return null;
                    }
                    if(closeParan>openParan+1) {
                        String req = request.substring(openParan+1,closeParan);
                        req = createTopOption(node,req);
                        createSubNode(node,req);
                    }
                    top.add(node);
                }
                if(offsetPutField>=0) {
                    fullFieldName = "putField";
                    Node node = new Node("putField");
                    int openParan = request.indexOf('(', offsetPutField);
                    int closeParan = request.indexOf(')', openParan);
                    if(closeParan == -1) {
                        message = request.substring(offsetField)
                                + " putField( does not have matching )";
                        return null;
                    }
                    if(closeParan>openParan+1) {
                        String req = request.substring(openParan+1,closeParan);
                        req = createTopOption(node,req);
                        createSubNode(node,req);
                    }
                    top.add(node);
                }
            } catch (IllegalStateException e) {
                message = "while creating Structure exception " + e.getMessage();
                return null;
            }
            int num = top.size();
            Field[] fields = new Field[num];
            String[] names = new String[num];
            for(int i=0; i<num; ++i) {
                Node node = top.get(i);
                names[i] = node.name;
                List<Node> subNode = node.nodes;
                if(subNode.isEmpty()) {
                    fields[i] = emptyStructure;
                } else {
                    fields[i] = createSubStructure(subNode);
                }
            }
            Structure structure = fieldCreate.createStructure(names, fields);
            PVStructure pvStructure = pvDataCreate.createPVStructure(structure);
            for(OptionPair pair: optionList) {
                String name = pair.name;
                String value = pair.value;
                PVString pvField = pvStructure.getSubField(PVString.class,name);
                pvField.put(value);
            }
            optionList.clear();
            return pvStructure;
        } catch (Exception e) {
            message = e.getMessage();
            return null;
        }
    }

    private int findMatchingBrace(String request,int index,int numOpen) {
        int openBrace = request.indexOf('{', index+1);
        int closeBrace = request.indexOf('}', index+1);
        if(openBrace==-1 && closeBrace==-1) {
            String message = "mismatched {} " + request;
            throw new IllegalStateException(message);
        }
        if(openBrace>0) {
            if(openBrace<closeBrace) return findMatchingBrace(request,openBrace,numOpen+1);
            if(numOpen==1) return closeBrace;
            return findMatchingBrace(request,closeBrace,numOpen-1);
        }
        if(numOpen==1) return closeBrace;
        return findMatchingBrace(request,closeBrace,numOpen-1);
    }

    private int findMatchingBracket(String request,int index) {
        for(int i=index+1; i< request.length(); ++i) {
            if(request.charAt(i) == ']') {
                if(i==index+1) {
                    message = " mismatched [ ]" + message;
                    throw new IllegalStateException(message);
                }
                return i;
            }
        }
        message = " missing ]" + request;
        throw new IllegalStateException(message);
    }

    private int findEndField(String request) {
        int ind = 0;
        int maxind = request.length() -1;
        while(true) {
            if(request.charAt(ind)==',') return ind;
            if(request.charAt(ind)=='[') {
                int closeBracket = findMatchingBracket(request,ind);
                if(closeBracket==-1) return closeBracket;
                ind = closeBracket;
                continue;
            }
            if(request.charAt(ind)=='{') {
                int closeBrace = findMatchingBrace(request,ind,1);
                if(ind>=request.length()) return request.length();
                ind = closeBrace;
                continue;
            }
            if(request.charAt(ind)=='.') {
                ++ind;
                continue;
            }
            if(ind>=maxind) break;
            ++ind;
        }
        return request.length();

    }


    private Node createRequestOptions(String request) {
        if(request.length()<=1) {
            throw new IllegalStateException("logic error empty options");
        }
        List<Node> top = new ArrayList<Node>();

        String[] items = commaPattern.split(request);
        int nitems = items.length;

        for(int j=0; j<nitems; j++) {
            String[] names = equalPattern.split(items[j]);
            if(names.length!=2) {
                message = "illegal option " + request;
                throw new IllegalStateException(message);
            }
            Node node = new Node(names[0]);
            String name = fullFieldName + "._options." + names[0];
            String value = names[1];
            optionList.add(new OptionPair(name,value));
            top.add(node);
        }
        Node node = new Node("_options");
        node.nodes = top;

        return node;
    }

    private String createTopOption(Node node,String request) {
        int length = request.length();
        if(length<=0) return request;
        char open = request.charAt(0);
        if(open!='[') return request;
        int end = 0;
        for(int i=0; i<length; ++i) {
            char chr= request.charAt(i);
            if(chr==']') {end = i; break;}
        }
        if(end==0)return request;
        String options = request.substring(1,end);
        Node optionNode = createRequestOptions(options);
        if(optionNode==null) return request;
        node.nodes.add(optionNode);
        if(end+1<length && request.charAt(end+1)==',') end = end+1;
        return request.substring(end+1) ;
    }

    private void createSubNode(Node node,String request) {
        int end = 0;

        for(int i=0; i<request.length(); ++i) {
            char chr= request.charAt(i);
            if(chr=='[') {end = i; break;}
            if(chr=='.') {end = i; break;}
            if(chr=='{') {end = i; break;}
            if(chr==',') {end = i; break;}
        }
        char chr = request.charAt(end);
        Node optionNode = null;
        if(chr=='[') {
            String saveFullName = fullFieldName;
            fullFieldName += "." + request.substring(0, end);
            int endBracket = findMatchingBracket(request,end);
            String options = request.substring(end+1,endBracket);
            optionNode = createRequestOptions(options);
            fullFieldName = saveFullName;
            int next = endBracket+1;
            if(next<request.length()) {
                request = request.substring(0, end) + request.substring(endBracket+1);
            } else {
                request = request.substring(0, end);
            }
            end = 0;
            for(int i=0; i<request.length(); ++i) {
                chr= request.charAt(i);
                if(chr=='.') {end = i; break;}
                if(chr=='{') {end = i; break;}
                if(chr==',') {end = i; break;}
            }
        }
        if(end==0) end = request.length();
        String name = request.substring(0, end);
        if(name.length()<1) {
            message = "null field name " + request;
            throw new IllegalStateException(message);
        }
        String saveFullName = fullFieldName;
        fullFieldName += "." + name;
        if(end==request.length()) {
            Node subNode = new Node(name);
            if(optionNode!=null) {
                subNode.nodes.add(optionNode);
            }
            node.nodes.add(subNode);
            fullFieldName = saveFullName;
            return;
        }
        if(chr==',') {
            Node subNode = new Node(name);
            if(optionNode!=null) {
                subNode.nodes.add(optionNode);
            }
            node.nodes.add(subNode);
            String rest = request.substring(end+1);
            fullFieldName = saveFullName;
            createSubNode(node,rest);
            return;
        }
        if(chr=='.') {
            request = request.substring(end+1);
            if(request.length()<1) {
                message = "null field name " + request;
                throw new IllegalStateException(message);
            }
            Node subNode = new Node(name);
            if(optionNode!=null) {
                subNode.nodes.add(optionNode);
            }
            int endField = findEndField(request);
            String subRequest = request.substring(0, endField);
            createSubNode(subNode,subRequest);
            node.nodes.add(subNode);
            int next = endField+1;
            if(next>=request.length()) {
                fullFieldName = saveFullName;
                return;
            }
            request = request.substring(next);
            fullFieldName = saveFullName;
            createSubNode(node,request);
            return;
        }
        if(chr=='{') {
            int endBrace = findEndField(request);
            if((end+1)>=(endBrace-1)) {
                message = " illegal syntax " + request;
                throw new IllegalStateException(message);
            }
            String subRequest = request.substring(end+1,endBrace-1);
            if(subRequest.length()<1) {
                message = " empty {} " + request;
                throw new IllegalStateException(message);
            }
            Node subNode = new Node(name);
            if(optionNode!=null) {
                subNode.nodes.add(optionNode);
            }
            createSubNode(subNode,subRequest);
            node.nodes.add(subNode);
            int next = endBrace + 1;
            if(next>=request.length()) {
                fullFieldName = saveFullName;
                return;
            }
            request = request.substring(next);
            fullFieldName = saveFullName;
            createSubNode(node,request);
            return;
        }
        message = "logic error";
        throw new IllegalStateException(message);
    }

    private Field createSubStructure(List<Node> nodes) {
        int num = nodes.size();
        Field[] fields = new Field[num];
        String[] names = new String[num];
        for(int i=0; i<num; ++i) {
            Node node = nodes.get(i);
            names[i] = node.name;
            if(node.name.equals("_options")) {
                fields[i] = createOptions(node.nodes);
            } else {
                List<Node> subNode = node.nodes;
                if(subNode.isEmpty()) {
                    fields[i] = emptyStructure;
                } else {
                    fields[i] = createSubStructure(subNode);
                }
            }
        }
        Structure structure = fieldCreate.createStructure(names, fields);
        return structure;
    }

    private Structure createOptions(List<Node> nodes) {
        int num = nodes.size();
        Field[] fields = new Field[num];
        String[] names = new String[num];
        for(int i=0; i<num; ++i) {
            Node node = nodes.get(i);
            names[i] = node.name;
            fields[i] = fieldCreate.createScalar(ScalarType.pvString);
        }
        Structure structure = fieldCreate.createStructure(names, fields);
        return structure;
    }

}
