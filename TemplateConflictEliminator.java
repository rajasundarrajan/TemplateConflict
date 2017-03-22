package com.Tool.Templateconflict;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * This TemplateConflictEliminator has method callMain() to get Stylesheet
 * filename as input and perform Template conflict removal and extracting the
 * Included stylesheets information
 * 
 * problem : the template with same match can be seen other SS file which
 * included in other SS files, which causes conflict in applying on the content
 * 
 * Solution : set priority value for the template match from its second
 * appearance onwards in the order the SS files are included, by which we
 * differentiate
 * 
 * templates by match and priority i.e,. though match is same in two templates
 * but priority differs.
 * 
 * Approach : Scan the master file for all the xsl:include and all the the
 * templates of the included SS under the xsl:include tag, repeat this step
 * until we do
 * 
 * replaced all the xsl:include even in the replaced contents. Scan through top
 * to bottom, identify duplicate match in templates, set priority from 1 to n
 * 
 * incrementally from the second appearance. write back to priority value of all
 * the templates in the corresponding files
 * 
 * 
 * @author Anustiya_C01
 *
 */
public class TemplateConflictEliminator {

	static String includeXSLInfo = "";

	private static boolean isincludeXSLInfoOnly = false;
	
	static ArrayList<PriorityDtls> tblVal = new ArrayList<PriorityDtls>();

	static PriorityDtls dtls = null;

	static Set<String> uniqueFiles = new HashSet<String>();

	/**
	 * overloaded method with default action as Template conflict elimination
	 * 
	 * @param file
	 */
	public static String callMain(String file) {
		return callMain(file, false);
		// return includeXSLInfo;

	}

	public static void main(String[] args) {
		// callMain("C:/raja/AnuTool/Template conflict/tempfiles/print-email-output1.xsl",
		// false, false);

		String[] files = new String[] {
				"C:\\RAJA\\AnuTool\\Template conflict\\anu\\email.xsl",
				"C:\\RAJA\\AnuTool\\Template conflict\\anu\\print.xsl" };

		callMain(files, false, false);
	}

	public static String callMain(String file, boolean includeXSLInfoOnly) {

		return callMain(new String[] { file }, includeXSLInfoOnly, false);
	}

	/**
	 * overloaded method with with specific action to show the Included
	 * Stylesheet information.
	 * 
	 * @param file
	 *            Master Stylesheet
	 * @param includeXSLInfoOnly
	 *            true indicates this call is to get included SS information
	 *            only
	 */
	public static String callMain(String[] files, boolean includeXSLInfoOnly,
			boolean processOutpuXSLfile) {

		try {

			/**
			 * Java reads the XSL file in XML way and converts the XSL tags
			 * contents to Document object this Document object has utility
			 * methods to extract the XML elements by name (it can be specific
			 * or all *)
			 */
			File stocks = new File(files[0]);

			isincludeXSLInfoOnly = includeXSLInfoOnly;

			includeXSLInfo = "";

			DocumentBuilderFactory dbFactory = DocumentBuilderFactory
					.newInstance();

			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();

			Document[] docs = new Document[files.length];

			File fileNew = new File(new File(files[0]).getParent()
					+ "\\tempfiles\\" + "combined_output.xsl");

			if (!fileNew.exists()) {
				fileNew.createNewFile();

			}
			
			FileWriter fw = new FileWriter(fileNew);
			fw.write("<xsl:stylesheet></xsl:stylesheet>");
			fw.close();

			Document doc = dBuilder.parse(fileNew);

			int cnt = 0;

			while (cnt < files.length) {

				stocks = new File(files[cnt]);

				docs[cnt] = dBuilder.parse(stocks);
				
				String[] tokens = files[cnt].split("[\\\\|/]");
				String filename = tokens[tokens.length - 1];
				
				NodeList nlist = docs[cnt].getElementsByTagName("xsl:template");
				
				for(int iter = 0; iter < nlist.getLength(); iter++){
					((Element)nlist.item(iter)).removeAttribute("priority");
					((Element)nlist.item(iter)).setAttribute("fileName", filename);
				}
				
				NodeList nodes = docs[cnt].getElementsByTagName("xsl:include");

				docs[cnt].getDocumentElement().normalize();

				String response = processStylesheets(nodes, stocks, docs[cnt]);


				
				addNodesNew(doc.getFirstChild(),
						docs[cnt].getElementsByTagName("xsl:template"),
						filename , "mainstylesheet");
				cnt++;
			}

			writeBackToXSL(fileNew.getCanonicalFile().getPath(), doc);
			/*
			 * if (processOutpuXSLfile) { // read all the XSL elements which is
			 * for xsl:include NodeList nodes =
			 * doc.getElementsByTagName("xsl:include");
			 * 
			 * // call to process all the stylesheets which are included in the
			 * // master stylesheet // processStylesheets(nodes, stocks, doc);
			 * String response = processStylesheets(nodes, stocks, doc);
			 * 
			 * if (response.equals("INVALID_SS_INCLUDED")) { return
			 * "INVALID_SS_INCLUDED"; }
			 * 
			 * // if the request is for included SS info, then write the SS //
			 * linking info into a file and exit if (includeXSLInfoOnly) {
			 * openLinksFile(stocks, includeXSLInfoOnly); return includeXSLInfo;
			 * }
			 * 
			 * }
			 */
			// by above we appended all the templates of included SS file to the
			// xsl:include

			// We need to set the priority to the templates where the match is
			// repeated from second time onwards
			
			writeBackToXSL(stocks.getParent() + "\\tempfiles//combined_raw_input.xsl"  , doc);
			
			readAllTemplatesSetPpriotity(doc, stocks, false);
			
			writeBackToXSL(stocks.getParent() + "\\tempfiles//combined_input.xsl"  , doc);
			
			reassignpriotiy(doc);
			
			writeBackToXSL(stocks.getParent() + "\\tempfiles//combined_output2.xsl"  , doc);
			
			tblVal.clear();
			
			readAllTemplatesSetPpriotity(doc, stocks, true);
			
			writeBackToXSL(stocks.getParent() + "\\tempfiles//combined_output3.xsl"  , doc);
			
			

			Iterator it = uniqueFiles.iterator();
			// this following code iterates all the files one by one, reads the
			// template details of each file and reads the priority of each
			// template
			// in the file then set the priority to the Document object

			// tblVal is the list of all the templates created from output.xsl
			if (!processOutpuXSLfile) {
				while (it.hasNext()) {
					String s = (String) it.next();
					// tmp is the filtered list of templates for the current
					// file
					ArrayList<PriorityDtls> tmp = (ArrayList<PriorityDtls>) tblVal
							.stream()
							.filter(obj -> obj.getFileName().equals(s))
							.collect(Collectors.toList());

					Document doc1 = dBuilder.parse(stocks.getParent() + "//"
							+ s);

					// read all the templates from the file
					NodeList ns = doc1.getElementsByTagName("xsl:template");
					Element e1;
					for (int i = 0; i < ns.getLength(); i++) {
						Node n1 = ns.item(i);
						e1 = ((Element) n1);

						String modeVal = getValue("mode", e1);

						String tmpName1 = getValue("match", e1)
								+ (StringUtils.isEmpty(modeVal) ? "" : "#"
										+ modeVal);

						String tmpName = tmpName1;

						if (StringUtils.isNotEmpty(tmpName)) {
							// pri is the priority of the current template of
							// the
							// file,
							Integer pri = 0;
							try {
								pri = ((PriorityDtls) (tmp
										.stream()
										.filter(tmpl -> tmpl.getTmpName()
												.endsWith(tmpName)).findFirst()
										.get())).getPriority();
							} catch (Exception e) {
								pri = 0;
							}
							// set pri as priority in the document only if pri >
							// 0,
							// which is saying we do not need priority tag for
							// the
							// first instance
							if (StringUtils.isNotEmpty(e1
									.getAttribute("priority"))) {
								if(Integer.parseInt(e1
										.getAttribute("priority")) > pri) {
									pri = Integer.parseInt(e1
											.getAttribute("priority"));
								}
							}
							if (pri > 0) {
								e1.setAttribute("priority", pri.toString());
							} else {
								e1.removeAttribute("priority");
							}
						}

					}
					// write the updated XML document back to the file.
					writeBackToXSL(stocks.getParent() + "//" + s, doc1);

				}
			}

			File theDir = new File(stocks.getParent() + "\\tempfiles\\");

			// if the directory does not exist, create it
			if (!theDir.exists()) {
				boolean result = false;
				try {
					theDir.mkdir();
					result = true;
				} catch (SecurityException se) {
				}
			}
			String fileName = stocks.getParent() + "\\tempfiles\\"
					+ "output.xsl";

			// write the XML document having all the tempates replaced for every
			// xsl:include in the main SS to the output.xsl file.
			writeBackToXSL(fileName, doc);

			openLinksFile(stocks, false);

			return includeXSLInfo;

		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return includeXSLInfo;
	}

	/**
	 * this function writes the SS inclusion details from master file to all,
	 * into the given input filename.
	 * 
	 * @param masterInputFile
	 *            file to write the SS inclusion details.
	 * @param forLinksOnly
	 */
	private static void openLinksFile(File masterInputFile, boolean forLinksOnly) {
		File f = masterInputFile.getParentFile();
		try {
			// if (!forLinksOnly) {
			// Desktop.getDesktop().open(f);
			// }
			System.out.println(includeXSLInfo);
			File theDir = new File(f.getAbsolutePath() + "\\tempfiles\\");

			// if the directory does not exist, create it
			if (!theDir.exists()) {
				boolean result = false;
				try {
					theDir.mkdir();
					result = true;
				} catch (SecurityException se) {
				}
			}
			// String fileName = f.getAbsolutePath() + "\\tempfiles\\" +
			// "output.xsl";

			File f1 = new File(f.getAbsolutePath() + "\\tempfiles\\"
					+ "fileLInks.txt");
			FileWriter fW1 = new FileWriter(f1);
			fW1.write(includeXSLInfo);
			fW1.flush();
			fW1.close();
			// Desktop.getDesktop().open(f1);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static String checkTmpMatch(String tmpWithMode, String tmpName,
			String modeVal) {
		if (tmpWithMode.contains("#")) {
			String[] splitVal = tmpWithMode.split("#");
			return tmpName.equals(splitVal[0]) && modeVal.equals(splitVal[1]) ? splitVal[0]
					: tmpWithMode;
		} else {
			return tmpWithMode;
		}
	}
	
	
	
	private static void reassignpriotiy(Document doc){
		
		NodeList nlist = doc.getElementsByTagName("xsl:template");
		
		Map<String, Integer> templatePriority = new HashMap<String, Integer>();
		
		for(int cnt = 0 ; cnt < nlist.getLength(); cnt++){
			
			String tempName = ((Element)nlist.item(cnt)).getAttribute("mainstylesheet") + "_" + ((Element)nlist.item(cnt)).getAttribute("match");
			
			int priority = Integer.parseInt(((Element)nlist.item(cnt)).getAttribute("priority"));
			
			if(templatePriority.containsKey(tempName)){
				
				if(templatePriority.get(tempName) < priority){
					templatePriority.put(tempName, priority);
				}
			}else{
				templatePriority.put(tempName, priority);
			}
		}
		
		for(int cnt = 0 ; cnt < nlist.getLength(); cnt++){
			
//			String tempName = ((Element)nlist.item(cnt)).getAttribute("match");
			
			String tempName = ((Element)nlist.item(cnt)).getAttribute("mainstylesheet") + "_" + ((Element)nlist.item(cnt)).getAttribute("match");
			
			((Element)nlist.item(cnt)).setAttribute("priority", templatePriority.get(tempName).toString());
		}
	}

	private static void readAllTemplatesSetPpriotity(Document doc, File stocks, boolean forMainSSCheck){
		
		Map<String, Integer> priorityMap = new HashMap<String, Integer>();

		NodeList nList = doc.getElementsByTagName("xsl:template");

		PriorityDtls dtls = null;

		// repeation of Template match [ex : match="x" ]is known by
		// following patterns

		// match = "x"

		// match = "a[x]"

		// match = "c | x"

		// match = a[x] | c[y]

		String testValue = "/*";

		for (int i = 0; i < nList.getLength(); i++) {

			Node node1 = nList.item(i);

			Element ele = (Element) node1;

			String tmpMatch = getValue("match", (ele));

			String mainSSName = getValue("mainstylesheet", (ele));

			
			String modeVal = getValue("mode", (ele));

			System.out.println(tmpMatch + "---"
					+ (StringUtils.isEmpty(modeVal) ? "" : "_" + modeVal));

			boolean isTmpMatchPiped = tmpMatch.contains("|");

			if (StringUtils.isNotEmpty(tmpMatch)) {

				if (tmpMatch.equals(testValue)) {
					System.out.println("testValue is reached");
				}

				String tmpMatch1 = "";
				
				
				// this handles match with Pipeline as well as @ attribute
				if (isTmpMatchPiped) {

					List<String> pipelinedTmps = new ArrayList<String>();

					for (String s : tmpMatch.split("\\|")) {
						s = s.trim();

						if (s.contains("lnv:CTTR-CONTENT-1")) {
							System.out.println("lnv:CTTR-CONTENT-1");
						}

						String attMatch = s.indexOf("[") >= 0
								&& s.indexOf("[") < s.indexOf("]") ? s
								.substring(0, s.indexOf("[")) : s
								+ (StringUtils.isEmpty(modeVal) ? "" : "#"
										+ modeVal);
						// ? s.substring(s.indexOf("[") + 1, s.indexOf("]"))
						// : s;

						tmpMatch1 = StringUtils.isEmpty(tmpMatch1) ? attMatch
								: tmpMatch1;

						if (tmpMatch1.contains("/")
								&& !tmpMatch1.startsWith("/")) {
							String[] tmpArr = tmpMatch1.split("/");
							tmpMatch1 = tmpArr[tmpArr.length - 1].trim();

							attMatch = tmpMatch1;
						}

						if(!forMainSSCheck){
							priorityMap.put((mainSSName + "_") + attMatch, (priorityMap
									.containsKey((mainSSName + "_") + attMatch) != true ? 
										(StringUtils.isNotEmpty(ele.getAttribute("priority")) ? Integer.parseInt(ele.getAttribute("priority")) : 1)
									: priorityMap.get((mainSSName + "_") + attMatch) + 1));
						}else{
							priorityMap.put((mainSSName + "_") + attMatch, ((StringUtils.isNotEmpty(ele
																.getAttribute("priority")) && (!priorityMap
																		.containsKey((mainSSName + "_")
																				+ attMatch) || Integer.parseInt(ele
																.getAttribute("priority")) > priorityMap
																.get((mainSSName + "_")
																		+ attMatch))) ? Integer.parseInt(ele
																.getAttribute("priority"))
																: (priorityMap
																		.get((mainSSName + "_")
																				+ attMatch) + 1)));
						}

						// priorityMap.put(tmpMatch1,
						// (priorityMap.containsKey(tmpMatch1) != true ? 1 :
						// priorityMap.get(tmpMatch1) + 1));

						pipelinedTmps.add(attMatch);

						if (priorityMap.containsKey((mainSSName + "_") + tmpMatch1)
								&& !tmpMatch1.equals(s)) {
							if (priorityMap.containsKey((mainSSName + "_") + s)
									&& priorityMap.get((mainSSName + "_") + tmpMatch1) < priorityMap
											.get((mainSSName + "_") + s)) {
								tmpMatch1 = s;
							}
						} else {
							tmpMatch1 = s;
						}

						if (tmpMatch1.contains("/")
								&& !tmpMatch1.startsWith("/")) {
							String[] tmpArr = tmpMatch1.split("/");
							tmpMatch1 = tmpArr[tmpArr.length - 1].trim();
						}
					}

					for (String str : pipelinedTmps) {
						priorityMap.put((mainSSName + "_") + str, priorityMap.get((mainSSName + "_") + tmpMatch1));
					}
				} else {
					// incase match has only attribute within it

					tmpMatch1 = (tmpMatch.indexOf("[") >= 0
							&& tmpMatch.indexOf("[") < tmpMatch
									.indexOf("]") ? tmpMatch.substring(0,
							tmpMatch.indexOf("["))
							: (isTmpMatchPiped ? tmpMatch1 : tmpMatch))
							+ (StringUtils.isEmpty(modeVal) ? "" : "#"
									+ modeVal);

					if (tmpMatch1.equalsIgnoreCase("jrnl-summary")
							|| tmpMatch.equals("jrnl-summary")) {
						System.out.println("dummy");
					}

					if (tmpMatch1.contains("/")
							&& !tmpMatch1.startsWith("/")) {
						String[] tmpArr = tmpMatch1.split("/");
						tmpMatch1 = tmpArr[tmpArr.length - 1].trim();
					}

/*					priorityMap.put((mainSSName + "_") + tmpMatch1, (priorityMap
							.containsKey((mainSSName + "_") + tmpMatch1) != true ? 1
							: priorityMap.get((mainSSName + "_") + tmpMatch1) + 1));
*/
					
					
					try{
					if(!forMainSSCheck){
						priorityMap.put((mainSSName + "_") + tmpMatch1, (priorityMap
								.containsKey((mainSSName + "_") + tmpMatch1) != true ? 
									(StringUtils.isNotEmpty(ele.getAttribute("priority")) ? Integer.parseInt(ele.getAttribute("priority")) : 1)
								: priorityMap.get((mainSSName + "_") + tmpMatch1) + 1));
					}else{
						priorityMap.put((mainSSName + "_") + tmpMatch1, ((StringUtils.isNotEmpty(ele
															.getAttribute("priority")) && (!priorityMap
																	.containsKey((mainSSName + "_")
																			+ tmpMatch1) || Integer.parseInt(ele
															.getAttribute("priority")) > priorityMap
															.get((mainSSName + "_")
																	+ tmpMatch1))) ? Integer.parseInt(ele
															.getAttribute("priority"))
															: (priorityMap
																	.get((mainSSName + "_")
																			+ tmpMatch1) + 1)));
					}
					}catch(Exception e){
						System.out.println(e.getMessage());
					}
					
				}
				// PriorityDTls holds all the values related to template,
				// like file, template, priority, once the prority is
				// updated
				// one object is created for each templates in the
				// results.xsl, and all those objects will be added to
				// tblVal list

				dtls = null;

				try {

					tmpMatch += (StringUtils.isEmpty(modeVal) ? "" : "#"
							+ modeVal);

					dtls = new PriorityDtls(
							(StringUtils.isNotEmpty(ele
									.getAttribute("fileName")) ? ele.getAttribute("fileName")
									: stocks.getName()),
							tmpMatch,
							(isTmpMatchPiped ? priorityMap.get((mainSSName + "_") + tmpMatch1) - 1
									: (priorityMap.containsKey((mainSSName + "_") + tmpMatch1) != true ? 0
											: priorityMap.get((mainSSName + "_") + tmpMatch1) - 1)), ele.getAttribute("mainstylesheet"));
				} catch (Exception e) {
					System.out.println("Error" + e.getMessage());
				}
				// have a reference of the unique filenames used in this
				// project
				uniqueFiles.add(dtls.getFileName());
				// list will hold all the objects created for each template
				tblVal.add(dtls);

				// set the priority to the doc element
				
				if(!forMainSSCheck){
				ele.setAttribute(
						"priority",
						""
								+ ((StringUtils.isNotEmpty(ele
										.getAttribute("priority")) ? Integer
										.parseInt(ele.getAttribute("priority"))
										: 0) + (isTmpMatchPiped ? priorityMap
										.get((mainSSName + "_") + tmpMatch1) - 1
										: (priorityMap
												.containsKey((mainSSName + "_")
														+ tmpMatch1) != true ? 0
												: priorityMap
														.get((mainSSName + "_")
																+ tmpMatch1) - 1))));
				}else{
				ele.setAttribute(
						"priority", "" + ((StringUtils.isNotEmpty(ele
												.getAttribute("priority")) && (Integer.parseInt(ele
												.getAttribute("priority")) >= priorityMap
												.get((mainSSName + "_")
														+ tmpMatch1))) ? Integer.parseInt(ele
												.getAttribute("priority"))
												: (priorityMap
														.get((mainSSName + "_")
																+ tmpMatch1))));
				}
				// update the priority to the dictionary, so that next value
				// will be used for next repeation of template
				/*
				 * if (!isTmpMatchPiped) { priorityMap.put(tmpMatch1,
				 * (priorityMap.containsKey(tmpMatch1) != true ? 1 :
				 * priorityMap.get(tmpMatch1) + 1)); }
				 */
				}

		}

	}
	
	/**
	 * this utility function helps us to write the XML document object into the
	 * file
	 * 
	 * @param fileName
	 *            file to write the XML Document
	 * @param doc
	 *            XML document object
	 */
	private static void writeBackToXSL(String fileName, Document doc) {

		try {
			Source source = new DOMSource(doc);

			File file = new File(fileName);
			Result result = new StreamResult(file);

			Transformer xformer = TransformerFactory.newInstance()
					.newTransformer();
			xformer.transform(source, result);
		} catch (Exception ex1) {

		}
	}

	/**
	 * this processStylesheets function processes all the inlcude nodes [nodes]
	 * and add all the templates from included SS under this xsl:include node
	 * once all the nodes are processed, this will start from top to bottom and
	 * processes new xsl:include added in the above and ends only when all the
	 * include nodes are processed
	 * 
	 * @param nodes
	 *            list XML elements of XSL:include in the file
	 * @param stocks
	 *            filename
	 * @param doc
	 *            XML Document
	 */
	private static String processStylesheets(NodeList nodes, File stocks,
			Document doc) {

		try {

			// iterate all the xsl:include nodes and replace them with templates
			// of the included SS file
			for (int i = 0; i < nodes.getLength(); i++) {

				Node node = nodes.item(i);

				if (node.getNodeType() == Node.ELEMENT_NODE
						&& node.getAttributes().getNamedItem("processed") == null) {
					Element element = (Element) node;
					element.setAttribute("processed", "true");
					// call utility method addNodes which will add all the
					// elements in the files[given in href] to this xsl:include
					// node
					node = addNodes(node, stocks.getParent(),
							getValue("href", element), "fileName").cloneNode(
							true);
					if (node == null) {
						return "INVALID_SS_INCLUDED";
					}

					String sourceFile = node.getAttributes().getNamedItem(
							"fileName") != null ? element
							.getAttribute("fileName") : stocks.getName();

					// reference to know which file includes which file
					includeXSLInfo += sourceFile + " ---includes--- "
							+ getValue("href", element) + "\n";

				}
			}
			// verify if there is any xsl:include after we replaced the
			// templates in place of xsl:include in the above for loop
			nodes = doc.getElementsByTagName("xsl:include");
			int nodeCnt = 0;
			for (int i = 0; i < nodes.getLength(); i++) {
				Node node = nodes.item(i);
				if (node.getAttributes().getNamedItem("processed") == null) {
					nodeCnt++;
				}

			}
			// repeat the process if any xsl:include yet to be processed, else
			// return back
			if (nodeCnt > 0) {
				String response = processStylesheets(nodes, stocks, doc);
				if (response.equals("INVALID_SS_INCLUDED")) {
					return "INVALID_SS_INCLUDED";
				}
			} else {
				return "";
			}
			return "";
		} catch (Exception e) {
			return "INVALID_SS_INCLUDED";
		}
	}

	/**
	 * simple utility method to read all the templates from the file [scope +
	 * file] and append to node [xsl : include]
	 * 
	 * @param node
	 *            Xsl:Include node
	 * @param scope
	 *            folder path
	 * @param file
	 *            inlucded filename to be processed
	 * @return xsl:include appended with templates from included SS file.
	 */
	private static Node addNodes(Node node, String scope, String file,
			String attrName) {

		try {
			File stocks = new File(scope + "\\" + file);
			if (stocks.exists() == false) {

				if (isincludeXSLInfoOnly) {
					return node;
				}
				// JOptionPane.showMessageDialog(null, "Included SS is not
				// available in the base folder");
				return null;
			}
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory
					.newInstance();

			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(stocks);
			doc.getDocumentElement().normalize();

			System.out.println("root of xml file"
					+ doc.getDocumentElement().getNodeName());
			Node nodes = doc.getDocumentElement();

			Node nodeNew = node.cloneNode(true);

			org.w3c.dom.NamedNodeMap nodesAll = doc.getAttributes();

			/*
			 * int cnt = 0;
			 * 
			 * NodeList listNodes = new no
			 * 
			 * while( cnt < nodesAll.getLength()){
			 * 
			 * }
			 */

			// addNodesNew(node, doc.getElementsByTagName("xsl:template"),
			// file);

			addNodesNew(node, doc.getElementsByTagName("*"), file, attrName);

			// addNodesNew(node, doc.getAttributes(), file);
			// doc.getElementsByTagName("xsl:include"), file);

			return node;

		} catch (Exception e) {
			System.out.println("Error occurred " + e.getMessage());
			return null;
		}

	}

	private static void addNodesNew(Node node, NodeList nList, String file,
			String attrName) {

		for (int i = 0; i < nList.getLength(); i++) {

			Node node1 = nList.item(i).cloneNode(false);
			if (node1.getNodeName().toLowerCase().equals("xsl:template")) {

				try{
					node.getOwnerDocument().adoptNode(node1);
				}catch(Exception e){
					System.out.println(e.getMessage());
				}

				((Element) node1).setAttribute(attrName, file);
				
				((Element) node1).removeAttribute("priority");

				node.appendChild(node1);
			}
		}

	}


/*	private static void addNodesNew1(Document node, NodeList nList, String file,
			String attrName) {

		for (int i = 0; i < nList.getLength(); i++) {

			Node node1 = nList.item(i).cloneNode(false);
			if (node1.getNodeName().toLowerCase().equals("xsl:template")) {

				try{
					node.getOwnerDocument().adoptNode(node1);
				}catch(Exception e){
					System.out.println(e.getMessage());
				}

				((Element) node1).setAttribute(attrName, file);

				node.appendChild(node1);
			}
		}

	}
*/

	// this is very simple utility method helps us to extract the attribute
	// value from the XML element.
	private static String getValue(String tag, Element element) {
		return element.getAttribute(tag);
	}

}
