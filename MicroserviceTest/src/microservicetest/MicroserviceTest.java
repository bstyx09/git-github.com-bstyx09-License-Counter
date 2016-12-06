/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package microservicetest;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Benjamin.Styx
 */
public class MicroserviceTest {

    static String textOutput;
    static Integer[][] finalLicenseCount;
    static List<String> systemProfileList = new ArrayList<String>();
    static List<String> agentTechs = new ArrayList<String>();

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
        //variable initialization
        int countOfJavaAgents = 0;
        int countOfMicroserviceAgents = 0;

        List<Integer> javaAgentCount = new ArrayList<Integer>();
        //List<Integer> agentTypeCount = new ArrayList<Integer>();
        List<Integer> javaMicroserviceCount = new ArrayList<Integer>();

        //open the XML file
        try (BufferedReader br = new BufferedReader(new FileReader(args[0]))) {
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();
            String entireFile = null;
            //pull everything from the file
            while (line != null) {
                entireFile = entireFile + line;
                line = br.readLine();
            }

            //new line for each different agent
            String[] agentInformation = entireFile.split("<agentinformation>");

            String[][] agentProperty = new String[agentInformation.length][];
            for (int i = 0; i < agentInformation.length; i++) {

                agentProperty[i] = agentInformation[i].split("><");

                //count the system profiles and agent technology types
                for (int j = 0; j < agentProperty[i].length; j++) {
                    if (agentInformation[i].contains("Instrumentation enabled")) {
                        //look for tech types and system profiles
                        String currentString = agentProperty[i][j];

                        if (currentString.contains("technologyType>")) {
                            String techType = currentString.substring(15, currentString.indexOf("<"));
                            if (agentTechs.isEmpty()) {
                                agentTechs.add(techType);
                            } else if (agentTechs.contains(techType)) {
                                //do nothing, this already exists!
                            } else {
                                agentTechs.add(techType);
                            }

                        } else if (currentString.contains("systemProfile>")) {

                            String systemProfileName = currentString.substring(14, currentString.indexOf("<"));
                            if (systemProfileList.isEmpty()) {
                                //
                                systemProfileList.add(systemProfileName);
                            } else if (systemProfileList.contains(systemProfileName)) {

                            } else {
                                systemProfileList.add(systemProfileName);
                            }
                        }
                    }
                }
            }

            agentTechs.add(("MicroService"));
            //initialize arrays based on number of system profiles and number of different types of technologies
            finalLicenseCount = new Integer[systemProfileList.size()][agentTechs.size()];
            for (int i = 0; i < systemProfileList.size(); i++) {
                javaMicroserviceCount.add(0);
                javaAgentCount.add(0);
                for (int j = 0; j < agentTechs.size(); j++) {
                    finalLicenseCount[i][j] = 0;
                }
            }

            for (int i = 0; i < agentInformation.length; i++) {
                /*
                 *
                 *
                 * look for java agents only
                 *
                 *
                 */
                if (agentInformation[i].contains("technologyType>Java</technologyType") && agentInformation[i].contains("Instrumentation enabled")) {
                    //new line for each property of each agent
                    countOfJavaAgents++;
                    boolean underMemThreshold = false;
                    //System.out.print(splitLineAgain[i][0]);
                    for (int j = 0; j < agentProperty[i].length; j++) {

                        //if (splitLineAgain[i][j].contains("agentHost>") || splitLineAgain[i][j].contains("agentInstanceName>") || splitLineAgain[i][j].contains("instrumentationState>") || splitLineAgain[i][j].contains("maximumMemory>")) {
                        if (agentProperty[i][j].contains("maximumMemory>")) {
                            String myString = agentProperty[i][j];
                            String memorySize = agentProperty[i][j].substring(14, (agentProperty[i][j].indexOf("<")));
                            double heapSize = Double.parseDouble(memorySize);
                            heapSize = (heapSize / 1073741824);
                            //System.out.println(heapSize);
                            if (heapSize <= 2) {
                                underMemThreshold = true;
                                countOfMicroserviceAgents++;
                                sb.append("Heap size = " + heapSize + "GB");
                                sb.append(System.lineSeparator());

                            } else {
                                sb.append("Heap size = " + heapSize + "GB");
                                sb.append(System.lineSeparator());

                            }
                        } else if (agentProperty[i][j].contains("agentInstanceName>")) {
                            String myString = agentProperty[i][j];
                            sb.append("Agent Name = " + myString.substring(18, myString.indexOf("<")));
                            sb.append(System.lineSeparator());

                        } else if (agentProperty[i][j].contains("instrumentationState>")) {
                            String myString = agentProperty[i][j];
                            //sb.append("Instrumentation State =  " + myString.substring(21, myString.indexOf("<")));
                            //sb.append(System.lineSeparator());

                        } else if (agentProperty[i][j].contains("systemProfile>")) {
                            String myString = agentProperty[i][j];
                            String systemProfileName = myString.substring(14, myString.indexOf("<"));

                           

                            sb.append("System Profile =  " + systemProfileName);
                            sb.append(System.lineSeparator());
                            sb.append(System.lineSeparator());

                            if (systemProfileList.isEmpty()) {
                                //Lists are empty
                                //New System Profile Name
                                systemProfileList.add(systemProfileName);
                                if (underMemThreshold) {
                                    javaMicroserviceCount.add(1);
                                    javaAgentCount.add(1);
                                } else {
                                    javaMicroserviceCount.add(0);
                                    javaAgentCount.add(1);
                                }
                            } else {
                                    //List is not empty

                                //see if this is already in the list
                                if (systemProfileList.contains(systemProfileName)) {
                                    int k = systemProfileList.indexOf(systemProfileName);
                                    //System Profile exists, increment it's number of agents

                                    if (underMemThreshold) {
                                        int previousValue = javaMicroserviceCount.get(k);
                                        javaMicroserviceCount.set(k, previousValue + 1);
                                        int previousValue2 = javaAgentCount.get(k);
                                        javaAgentCount.set(k, previousValue2 + 1);
                                        
                                        //add to real counter
                                        int microServiceIndex = agentTechs.indexOf("MicroService");
                                        int profileIndex = systemProfileList.indexOf(systemProfileName);
                                        int javaIndex = agentTechs.indexOf("Java");
                                        finalLicenseCount[profileIndex][javaIndex]++;
                                        finalLicenseCount[profileIndex][microServiceIndex]++;
                                    } else {
                                        int previousValue = javaAgentCount.get(k);
                                        javaAgentCount.set(k, previousValue + 1);
                                        int profileIndex = systemProfileList.indexOf(systemProfileName);
                                        int javaIndex = agentTechs.indexOf("Java");
                                        finalLicenseCount[profileIndex][javaIndex]++;
                                    }
                                } else {
                                    //New System Profile Name

                                    systemProfileList.add(systemProfileName);
                                    if (underMemThreshold) {
                                        javaMicroserviceCount.add(1);
                                        javaAgentCount.add(1);

                                    } else {
                                        javaMicroserviceCount.add(0);
                                        javaAgentCount.add(1);
                                    }
                                }

                            }

                        } else if (agentProperty[i][j].contains("agentGroup>")) {
                            String myString = agentProperty[i][j];
                            sb.append("Agent Group =  " + myString.substring(11, myString.indexOf("<")));
                            sb.append(System.lineSeparator());
                        }
                        //}
                    }
                } else if (agentInformation[i].contains("Instrumentation enabled")) {
                    /*
                     look for other types of agents
                     */
                    //check for each agent

                    String systemProfileName = null;
                    //look at each agent field
                    for (int m = 0; m < agentProperty[i].length; m++) {
                        String thisString = agentProperty[i][m];
                        //look for system profile name
                        if (thisString.contains("systemProfile>")) {
                            systemProfileName = thisString.substring(14, thisString.indexOf("<"));
                        }

                        //look for the technology type
                        if (thisString.contains("technologyType>")) {
                            String currentTech = thisString.substring(15, thisString.indexOf("<"));

                            int profileIndex = systemProfileList.indexOf(systemProfileName);
                            int techIndex = agentTechs.indexOf(currentTech);
                            finalLicenseCount[profileIndex][techIndex]++;
                                //
                            //if this isn't in the list already
                        }
                    }

                }

            }

            sb.append(System.lineSeparator());
            sb.append("--------------------------------------------------------------------------");
            sb.append(System.lineSeparator());
            sb.append("Java agent usage by system profile: ");
            sb.append(System.lineSeparator());
            sb.append("--------------------------------------------------------------------------");

            for (int i = 0; i < systemProfileList.size(); i++) {
                sb.append(System.lineSeparator());
                sb.append("System profile: " + systemProfileList.get(i));
                sb.append(System.lineSeparator());
                sb.append("Total Java agents: " + javaAgentCount.get(i));
                sb.append(System.lineSeparator());
                sb.append("Java agents under 2GB heap: " + javaMicroserviceCount.get(i));
                sb.append(System.lineSeparator());
            }
            sb.append(System.lineSeparator());
            sb.append(System.lineSeparator());
            sb.append("Number of Java agents = " + countOfJavaAgents);
            sb.append(System.lineSeparator());
            sb.append("Number of Java agents capable of using microservice licenses = " + countOfMicroserviceAgents);
            sb.append(System.lineSeparator());
            textOutput = sb.toString();
            System.out.print(textOutput);
            WriteToFile(args[0]);
            WriteToCSVFile(args[0]);
        }
    }

    public static void WriteToFile(String args) throws Exception {
        try {
            PrintWriter writer = new PrintWriter(args + "-output.txt", "UTF-8");
            writer.println(textOutput);
            writer.close();
        } catch (IOException e) {
            // do something
        }
    }

    public static void WriteToCSVFile(String args) throws Exception {
        try {
            PrintWriter writer = new PrintWriter(args + "-output.csv", "UTF-8");
            writer.print(" ,");
            for(int i = 0; i < agentTechs.size(); i++){
                writer.print(agentTechs.get(i) + ",");
            }
            writer.println();
            for(int i = 0; i < finalLicenseCount.length; i++){
                writer.print(systemProfileList.get(i) + ",");
                for(int j = 0; j < finalLicenseCount[i].length; j++){
                    writer.print(finalLicenseCount[i][j] + ",");
                }
                writer.println();
            }
            writer.close();
        } catch (Exception e) {
            // do something
        }
    }

}
