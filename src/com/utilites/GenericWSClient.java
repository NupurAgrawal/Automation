/******************************************************************************
* Licensed Materials - Property of IBM
*
* (C) Copyright IBM Corp. 2005, 2012 All Rights Reserved.
*
* US Government Users Restricted Rights - Use, duplication, or
* disclosure restricted by GSA ADP Schedule Contract with IBM Corp.
*
*****************************************************************************/
package com.utilites;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Hashtable;
import java.util.Map;

import javax.xml.namespace.QName;

import com.ibm.itim.ws.services.WSAccessService;
import com.ibm.itim.ws.services.WSAccessServiceService;
import com.ibm.itim.ws.services.WSAccountService;
import com.ibm.itim.ws.services.WSAccountServiceService;
import com.ibm.itim.ws.services.WSExtensionService;
import com.ibm.itim.ws.services.WSExtensionServiceService;
import com.ibm.itim.ws.services.WSOrganizationalContainerService;
import com.ibm.itim.ws.services.WSOrganizationalContainerServiceService;
import com.ibm.itim.ws.services.WSPasswordService;
import com.ibm.itim.ws.services.WSPasswordServiceService;
import com.ibm.itim.ws.services.WSPersonService;
import com.ibm.itim.ws.services.WSPersonServiceService;
import com.ibm.itim.ws.services.WSProvisioningPolicyService;
import com.ibm.itim.ws.services.WSProvisioningPolicyServiceService;
import com.ibm.itim.ws.services.WSRequestService;
import com.ibm.itim.ws.services.WSRequestServiceService;
import com.ibm.itim.ws.services.WSRoleService;
import com.ibm.itim.ws.services.WSRoleServiceService;
import com.ibm.itim.ws.services.WSSearchDataService;
import com.ibm.itim.ws.services.WSSearchDataServiceService;
import com.ibm.itim.ws.services.WSServiceService;
import com.ibm.itim.ws.services.WSServiceServiceService;
import com.ibm.itim.ws.services.WSSessionService;
import com.ibm.itim.ws.services.WSSessionService_Service;
import com.ibm.itim.ws.services.WSSharedAccessService;
import com.ibm.itim.ws.services.WSSharedAccessService_Service;
import com.ibm.itim.ws.services.WSSystemUserService;
import com.ibm.itim.ws.services.WSSystemUserServiceService;
import com.ibm.itim.ws.services.WSToDoService;
import com.ibm.itim.ws.services.WSToDoServiceService;
import com.ibm.itim.ws.services.WSUnauthService;
import com.ibm.itim.ws.services.WSUnauthServiceService;

public abstract class GenericWSClient {

	// Command line argument names prefixed by "-"
	static final String OPERATION_NAME = "add";
	static final String OPERATION_PARAMETERS = "parameters";
	static final String PARAM_ITIM_CREDENTIAL = "secret";
	static final String PARAM_ITIM_PRINCIPAL = "itim manager";
	static final String[][] utilParams = new String[][] { { OPERATION_NAME,	"No operationName specified" } };

	static String HOST = null;
	static String PORT = null;
	static final String WS_CONFIG_FILE = "/Properties/ws_config.properties";
	static final String WS_SUFFIX = "/itim/services/";
	static final String WS_WSDL_LOCATION = "/WEB-INF/wsdl/";
	static final String namespaceURI = "http://services.ws.itim.ibm.com";
	static boolean isCustomHostAndPortProvided = true;
	
	/*
	 * The static block below reads the ws_example_config.properties file 
	 * and sets the values of HOST and PORT */
	static {		
		java.util.Properties properties = new java.util.Properties();		
		try {
			properties.load(new FileInputStream(WS_CONFIG_FILE));
			//properties.getClass().getResourceAsStream(WS_CONFIG_FILE);
			System.out.println();
		} catch (Exception e1) {
			System.out.println("Error occurred while reading ("+ WS_CONFIG_FILE +")");
			e1.printStackTrace();
		}
		String host = properties.getProperty("host");
		String port = properties.getProperty("port");
		//System.out.println("***"+host+"***"+port);
		if( host!=null && !host.isEmpty()){
			HOST = host;
			//String port = properties.getProperty("port");
			if( port!=null && !port.isEmpty()){
				PORT = port;
			} else {
				isCustomHostAndPortProvided = false;			
			}
		} else {
			isCustomHostAndPortProvided = false;
		}		
	}

	/**
	 * @param serviceName
	 *            Name of the service
	 * @param serviceWSDLFileName
	 *            Name of WSDL file of service
	 * @return URL of the service specified in the input argument
	 */
	private static URL generateServiceURL(String serviceName, String serviceWSDLFileName){
	
		String strURL = "http://" + HOST + ":" + PORT + WS_SUFFIX + serviceName + WS_WSDL_LOCATION + serviceWSDLFileName;	
		//System.out.println("strURL="+strURL);
		URL url = null;
		try {
			url = new URL(strURL);			
		} catch (MalformedURLException e) {
			System.out.println("Failed to generate the wsdl resource URL");
			e.printStackTrace();
			System.exit(-1);
		}
		return url;
	}
	
	/**
	 * @return A Session service port on which the service's APIs can be
	 *         invoked.
	 */
	/*
protected static WSSessionService_Service getSessionService() {
		
	WSSessionService_Service service = null;
		URL url = null;		
		String serviceName = "WSSessionServiceService";
		
		if(!isCustomHostAndPortProvided){
			service = new WSSessionService_Service();	
		} else if ( (url = generateServiceURL(serviceName, "WSSessionService.wsdl") ) != null) {
			service = new WSSessionService_Service(url, new QName(namespaceURI, serviceName));			
		} else {			
			return null;
		}
		
		System.out.println(service.getWSSessionServicePort());
		WSSessionService_Service proxy = (WSSessionService_Service) service.getWSSessionServicePort();
		return proxy;			
	}
	*/
	protected static WSSessionService getSessionService() {
		
		WSSessionService_Service service = null;
		URL url = null;		
		String serviceName = "WSSessionService";
		
		if(!isCustomHostAndPortProvided){
			service = new WSSessionService_Service();	
		} else if ( (url = generateServiceURL(serviceName, "WSSessionService.wsdl") ) != null) {
			service = new WSSessionService_Service(url, new QName(namespaceURI, serviceName));			
		} else {			
			return null;
		}
		
		//System.out.println(service.getWSSessionServicePort());
		WSSessionService proxy = service.getWSSessionServicePort();
		return proxy;			
	}

	/**
	 * @return An Access Service port on which the service's APIs can be
	 *         invoked.
	 */
	protected static WSAccessService getAccessService() {
		
		WSAccessServiceService service = null;
		URL url = null;
		String serviceName = "WSAccessServiceService";

		if (!isCustomHostAndPortProvided) {
			service = new WSAccessServiceService();
		} else if ((url = generateServiceURL(serviceName,
				"WSAccessService.wsdl")) != null) {
			service = new WSAccessServiceService(url, new QName(namespaceURI,
					serviceName));
		} else {
			return null;
		}
		System.out.println(service.getWSAccessService());
		
		//WSAccessService proxy = service.getWSAccessService(features)
		WSAccessService proxy = service.getWSAccessService();
		return proxy;
	}
	
	/**
	 * @return An Account Service port on which the service's APIs can be
	 *         invoked.
	 */
	protected static WSAccountService getAccountService() {
		
		WSAccountServiceService service = null;
		URL url = null;
		String serviceName = "WSAccountServiceService";

		if (!isCustomHostAndPortProvided) {
			service = new WSAccountServiceService();
		} else if ((url = generateServiceURL(serviceName,
				"WSAccountService.wsdl")) != null) {
			service = new WSAccountServiceService(url, new QName(namespaceURI,
					serviceName));
		} else {
			return null;
		}
		WSAccountService proxy = service.getWSAccountService();
		return proxy;
	}
	
	/**
	 * @return An Organizational Container Service port on which the service's APIs can be
	 *         invoked.
	 */
	protected static WSOrganizationalContainerService getOrganizationalContainerService() {
		
		WSOrganizationalContainerServiceService service = null;
		URL url = null;
		String serviceName = "WSOrganizationalContainerServiceService";

		if (!isCustomHostAndPortProvided) {
			service = new WSOrganizationalContainerServiceService();
		} else if ((url = generateServiceURL(serviceName,
				"WSOrganizationalContainerService.wsdl")) != null) {
			service = new WSOrganizationalContainerServiceService(url,
					new QName(namespaceURI, serviceName));
		} else {
			return null;
		}
		WSOrganizationalContainerService proxy = service
				.getWSOrganizationalContainerServicePort();
		return proxy;
	}
	
	/**
	 * @return A Password Service port on which the service's APIs can be
	 *         invoked.
	 */
	protected static WSPasswordService getPasswordService() {
		
		WSPasswordServiceService service = null;
		URL url = null;
		String serviceName = "WSPasswordServiceService";

		if (!isCustomHostAndPortProvided) {
			service = new WSPasswordServiceService();
		} else if ((url = generateServiceURL(serviceName,
				"WSPasswordService.wsdl")) != null) {
			service = new WSPasswordServiceService(url, new QName(namespaceURI,
					serviceName));
		} else {
			return null;
		}
		WSPasswordService proxy = service.getWSPasswordService();
		return proxy;
	}
	
	/**
	 * @return A Person Service port on which the service's APIs can be
	 *         invoked.
	 */
	protected static WSPersonService getPersonService() {
		
		WSPersonServiceService service = null;
		URL url = null;
		String serviceName = "WSPersonServiceService";

		if (!isCustomHostAndPortProvided) {
			service = new WSPersonServiceService();
		} else if ((url = generateServiceURL(serviceName,
				"WSPersonService.wsdl")) != null) {
			service = new WSPersonServiceService(url, new QName(namespaceURI,
					serviceName));
		} else {
			return null;
		}
		WSPersonService proxy = service.getWSPersonService();
		return proxy;
	}
	
	/**
	 * @return A Provisioning Policy service port on which the service's APIs
	 *         can be invoked.
	 */
	protected static WSProvisioningPolicyService getProvisioningPolicyService() {
		
		WSProvisioningPolicyServiceService service = null;
		URL url = null;
		String serviceName = "WSProvisioningPolicyServiceService";

		if (!isCustomHostAndPortProvided) {
			service = new WSProvisioningPolicyServiceService();
		} else if ((url = generateServiceURL(serviceName,
				"WSProvisioningPolicyService.wsdl")) != null) {
			service = new WSProvisioningPolicyServiceService(url, new QName(
					namespaceURI, serviceName));
		} else {
			return null;
		}
		WSProvisioningPolicyService proxy = service
				.getWSProvisioningPolicyService();
		return proxy;
	}
	
	/**
	 * @return A Request Service port on which the service's APIs can be
	 *         invoked.
	 */
	protected static WSRequestService getRequestService() {
		
		WSRequestServiceService service = null;
		URL url = null;
		String serviceName = "WSRequestServiceService";

		if (!isCustomHostAndPortProvided) {
			service = new WSRequestServiceService();
		} else if ((url = generateServiceURL(serviceName,
				"WSRequestService.wsdl")) != null) {
			service = new WSRequestServiceService(url, new QName(namespaceURI,
					serviceName));
		} else {
			return null;
		}
		WSRequestService proxy = service.getWSRequestService();
		return proxy;
	}
	
	/**
	 * @return A Role Service port on which the service's APIs can be
	 *         invoked.
	 */
	protected static WSRoleService getRoleService() {
		
		WSRoleServiceService service = null;
		URL url = null;
		String serviceName = "WSRoleServiceService";

		if (!isCustomHostAndPortProvided) {
			service = new WSRoleServiceService();
		} else if ((url = generateServiceURL(serviceName, "WSRoleService.wsdl")) != null) {
			service = new WSRoleServiceService(url, new QName(namespaceURI,
					serviceName));
		} else {
			return null;
		}
		WSRoleService proxy = service.getWSRoleService();
		return proxy;
	}
	
	/**
	 * @return A Search Data Service port on which the service's APIs can be
	 *         invoked.
	 */
	protected static WSSearchDataService getSearchDataService() {
		
		WSSearchDataServiceService service = null;
		URL url = null;
		String serviceName = "WSSearchDataServiceService";

		if (!isCustomHostAndPortProvided) {
			service = new WSSearchDataServiceService();
		} else if ((url = generateServiceURL(serviceName,
				"WSSearchDataService.wsdl")) != null) {
			service = new WSSearchDataServiceService(url, new QName(
					namespaceURI, serviceName));
		} else {
			return null;
		}
		WSSearchDataService proxy = service.getWSSearchDataService();
		return proxy;
	}
	
	/**
	 * @return A Service service port on which the service's APIs can be
	 *         invoked.
	 */
	protected static WSServiceService getServiceService() {
		
		WSServiceServiceService service = null;
		URL url = null;
		String serviceName = "WSServiceServiceService";
		
		if(!isCustomHostAndPortProvided){	
			
			service = new WSServiceServiceService();			
		} else if ( (url = generateServiceURL(serviceName, "WSServiceService.wsdl") ) != null) {

			service = new WSServiceServiceService(url, new QName(namespaceURI, serviceName));			
		} else {
			
			return null;
		}	
		
		WSServiceService proxy = service.getWSServiceService();
		return proxy;			
	}
	
	/**
	 * @return A Shared Access service port on which the service's APIs can be
	 *         invoked.
	 */
	protected static WSSharedAccessService getSharedAccessService() {
		
		WSSharedAccessService_Service service = null;
		URL url = null;		
		String serviceName = "WSSharedAccessService";
	
		if(!isCustomHostAndPortProvided){
			service = new WSSharedAccessService_Service();			
		} else if ( (url = generateServiceURL(serviceName, "WSSharedAccessService.wsdl") ) != null) {
			service = new WSSharedAccessService_Service(url, new QName(namespaceURI, serviceName));			
		} else {			
			return null;
		}
		WSSharedAccessService proxy = service.getWSSharedAccess();
		return proxy;			
	}
	/**
	 * @return A System User service port on which the service's APIs can be
	 *         invoked.
	 */
	protected static WSSystemUserService getSystemUserService() {
		
		WSSystemUserServiceService service = null;
		URL url = null;
		String serviceName = "WSSystemUserServiceService";

		if (!isCustomHostAndPortProvided) {
			service = new WSSystemUserServiceService();
		} else if ((url = generateServiceURL(serviceName,
				"WSSystemUserService.wsdl")) != null) {
			service = new WSSystemUserServiceService(url, new QName(
					namespaceURI, serviceName));
		} else {
			return null;
		}
		WSSystemUserService proxy = service.getWSSystemUserService();
		return proxy;
	}
	
	/**
	 * @return A ToDo service port on which the service's APIs can be
	 *         invoked.
	 */
	protected static WSToDoService getToDoService() {
		
		WSToDoServiceService service = null;
		URL url = null;
		String serviceName = "WSToDoServiceService";

		if (!isCustomHostAndPortProvided) {
			service = new WSToDoServiceService();
		} else if ((url = generateServiceURL(serviceName, "WSToDoService.wsdl")) != null) {
			service = new WSToDoServiceService(url, new QName(namespaceURI,
					serviceName));
		} else {
			return null;
		}
		WSToDoService proxy = service.getWSToDoService();
		return proxy;
	}
	
	/**
	 * @return A UnauthService service port on which the service's APIs can be
	 *         invoked.
	 */
	protected static WSUnauthService getUnauthService() {

		WSUnauthServiceService service = null;
		URL url = null;
		String serviceName = "WSUnauthServiceService";

		if (!isCustomHostAndPortProvided) {
			service = new WSUnauthServiceService();
		} else if ((url = generateServiceURL(serviceName,
				"WSUnauthService.wsdl")) != null) {
			service = new WSUnauthServiceService(url, new QName(namespaceURI,
					serviceName));
		} else {
			return null;
		}
		WSUnauthService proxy = service.getWSUnauthService();
		return proxy;
	}

	/**
	 * @return A ExtensionService service port on which the service's APIs can be
	 *         invoked.
	 */
	protected static WSExtensionService getExtensionService() {

		WSExtensionServiceService service = null;
		
		URL url = null;
		String serviceName = "WSExtensionServiceService";

		if (!isCustomHostAndPortProvided) {
			service = new WSExtensionServiceService();
		} else if ((url = generateServiceURL(serviceName,
				"WSExtensionService.wsdl")) != null) {
			service = new WSExtensionServiceService(url, new QName(
					namespaceURI, serviceName));
		} else {
			return null;
		}
		WSExtensionService proxy = service.getWSExtensionService();
		return proxy;
	}
	
	public boolean run(String[] args, boolean verbose) {
		Utils utils = null;
		Hashtable<String, Object> arguments = null;		try {
			utils = new Utils(utilParams, verbose);
			arguments = utils.parseArgs(args);
		} catch (IllegalArgumentException ex) {
			if (verbose) {
				System.err.println(getUsage(ex.getMessage()));
			}
			return false;
		}
		executeOperation(arguments);
		return true;
	}

	public abstract boolean executeOperation(Map<String, Object> mpParams);

	public abstract String getUsage(String errMessage);

}
