/**
 * Copyright (c) 2011 Source Auditor Inc.
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
 *
*/
package org.spdx.rdfparser;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.spdx.spdxspreadsheet.InvalidLicenseStringException;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.util.FileManager;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

/**
 * Factory for creating SPDXLicenseInfo objects from a Jena model
 * @author Gary O'Neall
 *
 */
public class SPDXLicenseInfoFactory {
	
	static  final String[] STANDARD_LICENSE_IDS = new String[] {
		"AFL-1.1","AFL-1.2","AFL-2.0","AFL-2.1","AFL-3.0","APL-1.0",
		"Aladdin","ANTLR-PD","Apache-1.0","Apache-1.1","Apache-2.0",
		"APSL-1.0","APSL-1.1","APSL-1.2","APSL-2.0","Artistic-1.0",
		"Artistic-1.0-cl8","Artistic-1.0-Perl","Artistic-2.0","AAL",
		"BitTorrent-1.0","BitTorrent-1.1","BSL-1.0","BSD-2-Clause",
		"BSD-2-Clause-FreeBSD","BSD-2-Clause-NetBSD","BSD-3-Clause",
		"BSD-3-Clause-Clear","BSD-4-Clause","BSD-4-Clause-UC","CECILL-1.0",
		"CECILL-1.1","CECILL-2.0","CECILL-B","CECILL-C","ClArtistic",
		"CNRI-Python","CNRI-Python-GPL-Compatible","CPOL-1.02",
		"CDDL-1.0","CDDL-1.1","CPAL-1.0","CPL-1.0","CATOSL-1.1","Condor-1.1",
		"CC-BY-1.0","CC-BY-2.0","CC-BY-2.5","CC-BY-3.0","CC-BY-ND-1.0",
		"CC-BY-ND-2.0","CC-BY-ND-2.5","CC-BY-ND-3.0","CC-BY-NC-1.0",
		"CC-BY-NC-2.0","CC-BY-NC-2.5","CC-BY-NC-3.0","CC-BY-NC-ND-1.0",
		"CC-BY-NC-ND-2.0","CC-BY-NC-ND-2.5","CC-BY-NC-ND-3.0",
		"CC-BY-NC-SA-1.0","CC-BY-NC-SA-2.0","CC-BY-NC-SA-2.5","CC-BY-NC-SA-3.0",
		"CC-BY-SA-1.0","CC-BY-SA-2.0","CC-BY-SA-2.5","CC-BY-SA-3.0","CC0-1.0",
		"CUA-OPL-1.0","D-FSL-1.0","WTFPL","EPL-1.0","eCos-2.0","ECL-1.0",
		"ECL-2.0","EFL-1.0","EFL-2.0","Entessa","ErlPL-1.1","EUDatagrid",
		"EUPL-1.0","EUPL-1.1","Fair","Frameworx-1.0","FTL","AGPL-1.0",
		"AGPL-3.0","GFDL-1.1","GFDL-1.2","GFDL-1.3","GPL-1.0","GPL-1.0+",
		"GPL-2.0","GPL-2.0+","GPL-2.0-with-autoconf-exception",
		"GPL-2.0-with-bison-exception","GPL-2.0-with-classpath-exception",
		"GPL-2.0-with-font-exception","GPL-2.0-with-GCC-exception","GPL-3.0",
		"GPL-3.0+","GPL-3.0-with-autoconf-exception","GPL-3.0-with-GCC-exception",
		"LGPL-2.1","LGPL-2.1+","LGPL-3.0","LGPL-3.0+","LGPL-2.0","LGPL-2.0+",
		"gSOAP-1.3b","HPND","IBM-pibs","IPL-1.0","Imlib2","IJG","Intel","IPA",
		"ISC","JSON","LPPL-1.3a","LPPL-1.0","LPPL-1.1","LPPL-1.2","LPPL-1.3c",
		"Libpng","LPL-1.02","LPL-1.0","MS-PL","MS-RL","MirOS","MIT","Motosoto",
		"MPL-1.0","MPL-1.1","MPL-2.0","MPL-2.0-no-copyleft-exception","Multics",
		"NASA-1.3","Naumen","NBPL-1.0","NGPL","NOSL","NPL-1.0","NPL-1.1","Nokia",
		"NPOSL-3.0","NTP","OCLC-2.0","ODbL-1.0","PDDL-1.0","OGTSL","OLDAP-2.2.2",
		"OLDAP-1.1","OLDAP-1.2","OLDAP-1.3","OLDAP-1.4","OLDAP-2.0","OLDAP-2.0.1",
		"OLDAP-2.1","OLDAP-2.2","OLDAP-2.2.1","OLDAP-2.3","OLDAP-2.4","OLDAP-2.5",
		"OLDAP-2.6","OLDAP-2.7","OPL-1.0","OSL-1.0","OSL-2.0","OSL-2.1","OSL-3.0",
		"OLDAP-2.8","OpenSSL","PHP-3.0","PHP-3.01","PostgreSQL","Python-2.0",
		"QPL-1.0","RPSL-1.0","RPL-1.1","RPL-1.5","RHeCos-1.1","RSCPL","Ruby",
		"SAX-PD","SGI-B-1.0","SGI-B-1.1","SGI-B-2.0","OFL-1.0","OFL-1.1","SimPL-2.0",
		"Sleepycat","SMLNJ","SugarCRM-1.1.3","SISSL","SISSL-1.2","SPL-1.0","Watcom-1.0",
		"NCSA","VSL-1.0","W3C","WXwindows","Xnet","X11","XFree86-1.1","YPL-1.0","YPL-1.1",
		"Zimbra-1.3","Zlib","ZPL-1.1","ZPL-2.0","ZPL-2.1","Unlicense"
	};
	
	public static final String DEFAULT_LICENSE_LIST_VERSION = "1.19";
	static final Logger logger = Logger.getLogger(SPDXLicenseInfoFactory.class.getName());
	static final String STANDARD_LICENSE_ID_URL = "http://spdx.org/licenses/";
	
	public static final String NOASSERTION_LICENSE_NAME = "NOASSERTION";
	public static final String NONE_LICENSE_NAME = "NONE";

	public static final String STANDARD_LICENSE_URI_PREFIX = "http://spdx.org/licenses/";
	private static final String STANDARD_LICENSE_RDF_LOCAL_DIR = "resources" + "/" + "stdlicenses";

	private static final String STANDARD_LICENSE_RDF_LOCAL_FILENAME = STANDARD_LICENSE_RDF_LOCAL_DIR + "/" + "index.html";
	private static final String STANDARD_LICENSE_PROPERTIES_FILENAME = STANDARD_LICENSE_RDF_LOCAL_DIR + "/" + "licenses.properties";
	
	private static Model standardLicenseModel = null;
	
	static final HashSet<String> STANDARD_LICENSE_ID_SET = new HashSet<String>();
	
	static HashMap<String, SPDXStandardLicense> STANDARD_LICENSES = null;
    
	private static final Properties licenseProperties = loadLicenseProperties();
    private static final boolean onlyUseLocalLicenses = Boolean.parseBoolean(
            System.getProperty("SPDXParser.OnlyUseLocalLicenses", licenseProperties.getProperty("OnlyUseLocalLicenses", "false")));

    static String LICENSE_LIST_VERSION = DEFAULT_LICENSE_LIST_VERSION;
	static {
		loadStdLicenseIDs();		
	}
	
	static final HashSet<String> STANDARD_LICENSE_ID_URLS_SET = new HashSet<String>();
	
	static {
		for (int i = 0; i < STANDARD_LICENSE_IDS.length; i++) {
			STANDARD_LICENSE_ID_URLS_SET.add(STANDARD_LICENSE_ID_URL+STANDARD_LICENSE_IDS[i]);
		}	
	}
	
	/**
	 * Create the appropriate SPDXLicenseInfo from the model and node provided.
	 * The appropriate SPDXLicenseInfo subclass object will be chosen based on
	 * the class (rdf type) of the node.  If there is no rdf type, then the
	 * license ID is parsed to determine the type
	 * @param model
	 * @param node
	 * @return
	 */
	public static SPDXLicenseInfo getLicenseInfoFromModel(Model model, Node node) throws InvalidSPDXAnalysisException {
		if (!node.isURI() && !node.isBlank()) {
			throw(new InvalidSPDXAnalysisException("Can not create a LicenseInfo from a literal node"));
		}
		SPDXLicenseInfo retval = null;
		// check to see if it is a "standard" type of license (NONESEEN, NONE, NOTANALYZED, or STANDARD_LICENSE)
		if (node.isURI()) {
			retval = getLicenseInfoByUri(model, node);
		}
		if (retval == null) {	// try by type
			retval = getLicenseInfoByType(model, node);
		}
		if (retval == null) {	// try by ID
			retval = getLicenseInfoById(model, node);
		}
		if (retval == null) {	// OK, we give up
			logger.error("Could not determine the type for a license");
			throw(new InvalidSPDXAnalysisException("Could not determine the type for a license"));
		}
		return retval;
	}
	
	/**
	 * Obtains an SPDX license by a URI - could be a standard license or a predefined license type
	 * @param model
	 * @param node
	 * @return License Info for the license or NULL if no external standard license info could be found
	 * @throws InvalidSPDXAnalysisException 
	 */
	private static SPDXLicenseInfo getLicenseInfoByUri(Model model, Node node) throws InvalidSPDXAnalysisException {
		if (!node.isURI()) {
			return null;
		}
		if (node.getURI().equals(SpdxRdfConstants.SPDX_NAMESPACE+SpdxRdfConstants.TERM_LICENSE_NONE)) {
			return new SPDXNoneLicense(model, node);
		} else if (node.getURI().equals(SpdxRdfConstants.SPDX_NAMESPACE+SpdxRdfConstants.TERM_LICENSE_NOASSERTION)) {
			return new SpdxNoAssertionLicense(model, node);
		} else if (node.getURI().startsWith(STANDARD_LICENSE_URI_PREFIX)) {
			// try to fetch the standard license from the model
			try {
				return getLicenseFromStdLicModel(node.getURI());
			} catch (Exception ex) {
				logger.warn("Unable to get license from standard model for "+node.getURI());
				return null;
			}
		} else {
			return null;
		}
	}

	/**
	 * @param licenseId SPDX Standard License ID
	 * @return SPDX standard license
	 * @throws InvalidSPDXAnalysisException
	 */
	public static SPDXStandardLicense getStandardLicenseById(String licenseId)throws InvalidSPDXAnalysisException {
		return getLicenseFromStdLicModel(STANDARD_LICENSE_URI_PREFIX + licenseId);
	}
	
	/**
	 * @param uri
	 * @return
	 * @throws InvalidSPDXAnalysisException 
	 */
	protected static SPDXStandardLicense getLicenseFromStdLicModel(String uri) throws InvalidSPDXAnalysisException {
		URL licenseUrl = null;
		try {
			licenseUrl = new URL(uri);
		} catch (MalformedURLException e) {
			throw new InvalidSPDXAnalysisException("Invalid standard license URL: "+e.getMessage());
		}
		String[] pathParts = licenseUrl.getFile().split("/");
		String id = pathParts[pathParts.length-1];
		if (STANDARD_LICENSES.containsKey(id)) {
			return STANDARD_LICENSES.get(id);
		}
		String base = STANDARD_LICENSE_ID_URL + id;
		Model licenseModel = getLicenseModel(uri, base);
		if (licenseModel == null) {
			throw(new InvalidSPDXAnalysisException("No standard license was found at "+uri));
		}
		Resource licResource = licenseModel.getResource(base);
		if (licResource == null || !licenseModel.containsResource(licenseModel.asRDFNode(licResource.asNode()))) {
			throw(new InvalidSPDXAnalysisException("No standard license was found at "+uri));
		}
		SPDXStandardLicense retval = new SPDXStandardLicense(licenseModel, licResource.asNode());
		STANDARD_LICENSES.put(id, retval);
		return retval;
	}

	/**
	 * @param uri - URI of the actual resource
	 * @param base - base for any fragments present in the license model
	 * @return
	 * @throws NoStandardLicenseRdfModel 
	 */
	private static Model getLicenseModel(String uri, String base) throws NoStandardLicenseRdfModel {
		try {
			Class.forName("net.rootdev.javardfa.jena.RDFaReader");
		} catch(java.lang.ClassNotFoundException e) {
			throw(new NoStandardLicenseRdfModel("Could not load the RDFa reader for licenses.  This could be caused by an installation problem - missing java-rdfa jar file"));
		}  
		Model retval = ModelFactory.createDefaultModel();
		InputStream in = null;
		try {
			try {
				if (!onlyUseLocalLicenses) {
				    in = FileManager.get().open(uri);
					try {
						retval.read(in, base, "HTML");
						Property p = retval.getProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_LICENSE_ID);
				    	if (retval.isEmpty() || !retval.contains(null, p)) {
					    	try {
								in.close();
							} catch (IOException e) {
								logger.warn("Error closing standard license input");
							}
					    	in = null;
				    	}
					} catch(Exception ex) {

						if (in != null) {
							in.close();
							in = null;
						}
					}
				}
			} catch(Exception ex) {
				in = null;
				logger.warn("Unable to open SPDX standard license model.  Using local file copy for standard licenses");
			}
			if (in == null) {
				// need to fetch from the local file system
				String id = uri.substring(STANDARD_LICENSE_URI_PREFIX.length());
				String fileName = STANDARD_LICENSE_RDF_LOCAL_DIR + "/" + id;
				in = SPDXLicenseInfoFactory.class.getResourceAsStream("/" + fileName);
				if (in == null) {
					throw(new NoStandardLicenseRdfModel("Standard license "+uri+" could not be read."));
				}
				try {
					retval.read(in, base, "HTML");
				} catch(Exception ex) {
					throw(new NoStandardLicenseRdfModel("Error reading the standard licenses: "+ex.getMessage(),ex));
				}
			}
			return retval;
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					logger.warn("Unable to close model input stream");
				}
			}
		}
	}
	
	private static Model getStandardLicenseModel() throws InvalidSPDXAnalysisException {
		if (standardLicenseModel == null) {
			loadStandardLicenseModel();
		}
		return standardLicenseModel;
	}

	/**
	 * Load a standard license model from the index page
	 */
	private static void loadStandardLicenseModel() throws InvalidSPDXAnalysisException {
		try {
			Class.forName("net.rootdev.javardfa.jena.RDFaReader");
		} catch(java.lang.ClassNotFoundException e) {
			logger.warn("Unable to load Java RDFa reader");
		}  

		Model myStdLicModel = ModelFactory.createDefaultModel();	// don't use the static model to remove any possible timing windows while we are creating
		String fileType = "HTML";
		String base = STANDARD_LICENSE_URI_PREFIX+"index.html";
		InputStream licRdfInput;
		if (onlyUseLocalLicenses) {
		    licRdfInput = null;
		} else {
		    licRdfInput = FileManager.get().open(STANDARD_LICENSE_URI_PREFIX+"index.html");
		    try {
		    	myStdLicModel.read(licRdfInput, base, fileType);
				Property p = myStdLicModel.getProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_LICENSE_ID);
		    	if (myStdLicModel.isEmpty() || !myStdLicModel.contains(null, p)) {
			    	try {
						licRdfInput.close();
					} catch (IOException e) {
						logger.warn("Error closing standard license input");
					}
			    	licRdfInput = null;
		    	}
		    } catch(Exception ex) {	    	
	    		logger.warn("Unable to access the SPDX standard licenses at http://www.spdx.org/licenses.  Using local file copy of standard licenses");
	    		if (licRdfInput != null) {
	    			try {
	    				licRdfInput.close();
	    			} catch (IOException e) {
	    				logger.warn("Error closing standard license input");
	    			}
	    			licRdfInput = null;	
	    		}
	    	}
	    }	
		try {
			if (licRdfInput == null) {
				// need to load a static copy
				base = "file://"+STANDARD_LICENSE_RDF_LOCAL_FILENAME;
				licRdfInput = FileManager.get().open(STANDARD_LICENSE_RDF_LOCAL_FILENAME);
				if ( licRdfInput == null ) {
					// try the class loader
					licRdfInput = SPDXLicenseInfoFactory.class.getResourceAsStream("/" + STANDARD_LICENSE_RDF_LOCAL_FILENAME);
				}
				if (licRdfInput == null) {
					throw new NoStandardLicenseRdfModel("Unable to open standard license from website or from local file");
				}
				try {
					myStdLicModel.read(licRdfInput, base, fileType);
				} catch(Exception ex) {
					throw new NoStandardLicenseRdfModel("Unable to read the standard license model", ex);
				}
			}

			standardLicenseModel = myStdLicModel;	
		} finally {
			if (licRdfInput != null) {
				try {
					licRdfInput.close();
				} catch (IOException e) {
					logger.warn("Unable to close license RDF Input Stream");
				}
			}
		}
	}
	
	static void loadStdLicenseIDs() {
		STANDARD_LICENSES = new HashMap<String, SPDXStandardLicense>();
		try {
			Model stdLicenseModel = getStandardLicenseModel();
			Node p = stdLicenseModel.getProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_LICENSE_ID).asNode();
			Triple m = Triple.createMatch(null, p, null);
			ExtendedIterator<Triple> tripleIter = stdLicenseModel.getGraph().find(m);	
			while (tripleIter.hasNext()) {
				Triple t = tripleIter.next();
				STANDARD_LICENSE_ID_SET.add(t.getObject().toString(false));
			}
			p = stdLicenseModel.getProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_LICENSE_LIST_VERSION).asNode();
			m = Triple.createMatch(null, p, null);
			tripleIter = stdLicenseModel.getGraph().find(m);	
			if (tripleIter.hasNext()) {
				Triple t = tripleIter.next();
				LICENSE_LIST_VERSION = t.getObject().toString(false);
			}
		} catch (Exception ex) {
			logger.warn("Error loading standard license ID's from model.  Using static standard license ID's");
			for (int i = 0; i < STANDARD_LICENSE_IDS.length; i++) {
				STANDARD_LICENSE_ID_SET.add(STANDARD_LICENSE_IDS[i]);
			}	
		}
	}

	/**
	 * @param model
	 * @param node
	 * @return
	 * @throws InvalidSPDXAnalysisException 
	 */
	private static SPDXLicenseInfo getLicenseInfoById(Model model, Node node) throws InvalidSPDXAnalysisException {
		Node licenseIdPredicate = model.getProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_LICENSE_ID).asNode();
		Triple m = Triple.createMatch(node, licenseIdPredicate, null);
		ExtendedIterator<Triple> tripleIter = model.getGraph().find(m);
		if (tripleIter.hasNext()) {
			Triple triple = tripleIter.next();
			String id = triple.getObject().toString(false);
			if (tripleIter.hasNext()) {
				throw(new InvalidSPDXAnalysisException("More than one ID associated with license "+id));
			}
			if (isStandardLicenseID(id)) {
				return new SPDXStandardLicense(model, node);
			} else if (id.startsWith(SpdxRdfConstants.NON_STD_LICENSE_ID_PRENUM)) {
				return new SPDXNonStandardLicense(model, node);
			} else {
				// could not determine the type from the ID
				// could be a conjunctive or disjunctive license ID
				return null;
			}
		} else {
			throw(new InvalidSPDXAnalysisException("No ID associated with a license"));
		}
	}

	/**
	 * @param model
	 * @param node
	 * @return
	 * @throws InvalidSPDXAnalysisException 
	 */
	private static SPDXLicenseInfo getLicenseInfoByType(Model model, Node node) throws InvalidSPDXAnalysisException {
		// find the subclass
		Node rdfTypePredicate = model.getProperty(SpdxRdfConstants.RDF_NAMESPACE, 
				SpdxRdfConstants.RDF_PROP_TYPE).asNode();
		Triple m = Triple.createMatch(node, rdfTypePredicate, null);
		ExtendedIterator<Triple> tripleIter = model.getGraph().find(m);	// find the type(s)
		if (tripleIter.hasNext()) {
			Triple triple = tripleIter.next();
			if (tripleIter.hasNext()) {
				throw(new InvalidSPDXAnalysisException("More than one type associated with a licenseInfo"));
			}
			Node typeNode = triple.getObject();
			if (!typeNode.isURI()) {
				throw(new InvalidSPDXAnalysisException("Invalid type for licenseInfo - not a URI"));
			}
			// need to parse the URI
			String typeUri = typeNode.getURI();
			if (!typeUri.startsWith(SpdxRdfConstants.SPDX_NAMESPACE)) {
				throw(new InvalidSPDXAnalysisException("Invalid type for licenseInfo - not an SPDX type"));
			}
			String type = typeUri.substring(SpdxRdfConstants.SPDX_NAMESPACE.length());
			if (type.equals(SpdxRdfConstants.CLASS_SPDX_CONJUNCTIVE_LICENSE_SET)) {
				return new SPDXConjunctiveLicenseSet(model, node);
			} else if (type.equals(SpdxRdfConstants.CLASS_SPDX_DISJUNCTIVE_LICENSE_SET)) {
				return new SPDXDisjunctiveLicenseSet(model, node);
			}else if (type.equals(SpdxRdfConstants.CLASS_SPDX_EXTRACTED_LICENSING_INFO)) {
				return new SPDXNonStandardLicense(model, node);
			}else if (type.equals(SpdxRdfConstants.CLASS_SPDX_STANDARD_LICENSE)) {
				return new SPDXStandardLicense(model, node);
			} else {
				throw(new InvalidSPDXAnalysisException("Invalid type for licenseInfo '"+type+"'"));
			}
		} else {
			return null;
		}
	}

	/**
	 * Parses a license string and converts it into a SPDXLicenseInfo object
	 * Syntax - A license set must start and end with a parenthesis "("
	 * 			A conjunctive license set will have and AND after the first
	 *				licenseInfo term
	 * 			A disjunctive license set will have an OR after the first 
	 *				licenseInfo term
	 *			If there is no And or Or, then it is converted to a simple
	 *				license type (standard or non-standard)
	 *			A space or tab must be used between license ID's and the 
	 *				keywords AND and OR
	 *			A licenseID must NOT be "AND" or "OR"
	 * @param licenseString String conforming to the syntax
	 * @return an SPDXLicenseInfo created from the string
	 * @throws InvalidLicenseStringException 
	 */
	public static SPDXLicenseInfo parseSPDXLicenseString(String licenseString) throws InvalidLicenseStringException {
		String parseString = licenseString.trim();
		if (parseString.isEmpty()) {
			throw(new InvalidLicenseStringException("Empty license string"));
		}
		if (parseString.charAt(0) == '(') {
			if (!parseString.endsWith(")")) {
				throw(new InvalidLicenseStringException("Missing end ')'"));
			}
			// this will be treated some form of License Set
			parseString = parseString.substring(1, parseString.length()-1).trim();
			return parseLicenseSet(parseString);
		} else {
			// this is either a standard license or a non-standard license
			int startOfIDPos = skipWhiteSpace(parseString, 0);
			int endOfIDPos = skipNonWhiteSpace(parseString, startOfIDPos);
			String licenseID = parseString.substring(startOfIDPos, endOfIDPos);
			if (licenseID.equals(NONE_LICENSE_NAME)) {
				return new SPDXNoneLicense();
			} else if (licenseID.equals(NOASSERTION_LICENSE_NAME)) {
				return new SpdxNoAssertionLicense();
			} 
			if (isStandardLicenseID(licenseID)) {
				try {
					return getStandardLicenseById(licenseID);
                } catch (InvalidSPDXAnalysisException e) {
                    throw new InvalidLicenseStringException(e.getMessage());
                }
			} else {
				return new SPDXNonStandardLicense(licenseID, null);
			}
		}
	}

	/**
	 * Parses a license set which consists of a list of LicenseInfo strings
	 * @param parseString
	 * @return
	 * @throws InvalidLicenseStringException 
	 */
	private static SPDXLicenseInfo parseLicenseSet(String parseString) throws InvalidLicenseStringException {
		boolean isConjunctive = false;
		boolean isDisjunctive = false;
		ArrayList<SPDXLicenseInfo> licenseInfoList = new ArrayList<SPDXLicenseInfo>();
		int pos = 0;	// character position
		while (pos < parseString.length()) {
			// skip white space
			pos = skipWhiteSpace(parseString, pos);
			if (pos >= parseString.length()) {
				break;	// we are done
			}
			// collect the license information
			if (parseString.charAt(pos) == '(') {
				int startOfSet = pos + 1;
				pos = findEndOfSet(parseString, pos);
				if (pos > parseString.length() || parseString.charAt(pos) != ')') {
					throw(new InvalidLicenseStringException("Missing end ')'"));
				}
				licenseInfoList.add(parseLicenseSet(parseString.substring(startOfSet, pos)));
				pos++;
			} else {
				// a license ID
				int startOfID = pos;
				pos = skipNonWhiteSpace(parseString, pos);
				String licenseID = parseString.substring(startOfID, pos);
				if (licenseID.equals(NONE_LICENSE_NAME)) {
					licenseInfoList.add(new SPDXNoneLicense());
				} else if (licenseID.equals(NOASSERTION_LICENSE_NAME)) {
					licenseInfoList.add(new SpdxNoAssertionLicense());
				} 
				if (isStandardLicenseID(licenseID)) {
					try {
						licenseInfoList.add(getStandardLicenseById(licenseID));
                    } catch (InvalidSPDXAnalysisException e) {
                        throw new InvalidLicenseStringException(e.getMessage());
                    }
				} else {
					licenseInfoList.add(new SPDXNonStandardLicense(licenseID, null));
				}
			}
			if (pos >= parseString.length()) {
				break;	// done
			}
			// consume the AND or the OR
			// skip more whitespace
			pos = skipWhiteSpace(parseString, pos);
			if (parseString.charAt(pos) == 'A' || parseString.charAt(pos) == 'a') {
				// And
				if (pos + 4 >= parseString.length() || 
						!parseString.substring(pos, pos+4).toUpperCase().equals("AND ")) {
					throw(new InvalidLicenseStringException("Expecting an AND"));
				}
				isConjunctive = true;
				pos = pos + 4;
			} else if (parseString.charAt(pos) == 'O' || parseString.charAt(pos) == 'o') {
				// or
				if (pos + 3 >= parseString.length() || 
						!parseString.substring(pos, pos+3).toUpperCase().equals("OR ")) {
					throw(new InvalidLicenseStringException("Expecting an OR"));
				}
				isDisjunctive = true;
				pos = pos + 3;
			} else {
				throw(new InvalidLicenseStringException("Expecting an AND or an OR"));
			}
		}
		if (isConjunctive && isDisjunctive) {
			throw(new InvalidLicenseStringException("Can not have both AND's and OR's inside the same set of parenthesis"));
		}
		SPDXLicenseInfo[] licenseInfos = new SPDXLicenseInfo[licenseInfoList.size()];
		licenseInfos = licenseInfoList.toArray(licenseInfos);
		if (isConjunctive) {
			return new SPDXConjunctiveLicenseSet(licenseInfos);
		} else if (isDisjunctive) {
			return new SPDXDisjunctiveLicenseSet(licenseInfos);
		} else {
			throw(new InvalidLicenseStringException("Missing AND or OR inside parenthesis"));
		}
	}

	/**
	 * @param parseString
	 * @return
	 * @throws InvalidLicenseStringException 
	 */
	private static int findEndOfSet(String parseString, int pos) throws InvalidLicenseStringException {
		if (parseString.charAt(pos) != '(') {
			throw(new InvalidLicenseStringException("Expecting '('"));
		}
		int retval = pos;
		retval++;
		while (retval < parseString.length() && parseString.charAt(retval) != ')') {
			if (parseString.charAt(retval) == '(') {
				retval = findEndOfSet(parseString, retval) + 1;
			} else {
				retval++;
			}
		}
		return retval;
	}

	/**
	 * @param parseString
	 * @param pos
	 * @return
	 */
	private static int skipWhiteSpace(String parseString, int pos) {
		int retval = pos;
		char c = parseString.charAt(retval);
		while (retval < parseString.length() &&
				(c == ' ' || c == '\t' || c == '\r' || c == '\n')) {
			retval++;
			if (retval < parseString.length()) {
				c = parseString.charAt(retval);
			}
		}
		return retval;
	}
	
	/**
	 * @param parseString
	 * @param pos
	 * @return
	 */
	private static int skipNonWhiteSpace(String parseString, int pos) {
		int retval = pos;
		char c = parseString.charAt(retval);
		while (retval < parseString.length() &&
				c != ' ' && c != '\t' && c != '\r' && c != '\n') {
			retval++;
			if (retval < parseString.length()) {
				c = parseString.charAt(retval);
			}
		}
		return retval;
	}

	/**
	 * @param licenseID
	 * @return true if the licenseID belongs to a standard license
	 * @throws InvalidSPDXAnalysisException 
	 */
	public static boolean isStandardLicenseID(String licenseID)  {
		return STANDARD_LICENSE_ID_SET.contains(licenseID);
	}

	/**
	 * Tries to load properties from STANDARD_LICENSE_PROPERTIES_FILENAME, ignoring errors
	 * encountered during the process (e.g., the properties file doesn't exist, etc.).
	 * 
	 * @return a (possibly empty) set of properties
	 */
	private static Properties loadLicenseProperties() {
        Properties licenseProperties = new Properties();
        InputStream in = null;
        try {
            in = SPDXLicenseInfoFactory.class.getResourceAsStream("/" + STANDARD_LICENSE_PROPERTIES_FILENAME);
            licenseProperties.load(in);
        } catch (IOException e) {
            // Ignore it and fall through
        	logger.warn("IO Exception reading standard license properties file: "+e.getMessage());
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                   logger.warn("Unable to close standard license properties file: "+e.getMessage());
                }
            }
        }
        return licenseProperties;
    }
	
	
	/**
	 * @return Array of all standard license IDs
	 */
	public static String[] getStandardLicenseIds() {
		return STANDARD_LICENSE_ID_SET.toArray(new String[STANDARD_LICENSE_ID_SET.size()]);
	}
	
	/**
	 * @return Version of the license list being used by the SPDXLicenseInfoFactory
	 */
	public static String getLicenseListVersion() {
		return LICENSE_LIST_VERSION;
	}   
}
