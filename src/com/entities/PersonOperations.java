package com.entities;
import com.utilites.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import javax.xml.datatype.XMLGregorianCalendar;
import com.ibm.itim.dataservices.model.ObjectProfileCategory;
import com.ibm.itim.ws.model.WSAccount;
import com.ibm.itim.ws.model.WSAttribute;
import com.ibm.itim.ws.model.WSOrganizationalContainer;
import com.ibm.itim.ws.model.WSPerson;
import com.ibm.itim.ws.model.WSRequest;
import com.ibm.itim.ws.model.WSRole;
import com.ibm.itim.ws.model.WSSession;
import com.ibm.itim.ws.services.ArrayOfTns1WSAttribute;
import com.ibm.itim.ws.services.ArrayOfXsdString;
import com.ibm.itim.ws.services.WSLoginServiceException;
import com.ibm.itim.ws.services.WSOrganizationalContainerService;
import com.ibm.itim.ws.services.WSPersonService;
import com.ibm.itim.ws.services.WSRequestService;
import com.ibm.itim.ws.services.WSRoleService;
import com.ibm.itim.ws.services.WSSessionService;



public class PersonOperations extends GenericWSClient{
	//------------------Attributes Declaration------------------------------
	private static final String principal = "itim manager";
	private static final String credential = "secret";
	private static final String PARAM_ORG_CONTAINER = "idm";
	private static final String PARAM_PERSON_FILTER = "(cn=RahulV)";
	private static final String PARAM_PERSON_FILTER_ROLE= "(cn=Akash Nikam)";
	private static final String PARAM_PERSON_FILTER_MODIFY = "(cn=Akash Nikam)";
	private static final String PARAM_ROLE_FILTER = "(errolename=dev)";
	private static final String PARAM_DATE = "22/02/2017";
	private static final String PARAM_INCLUDE_ACCOUNTS = "true";
	private static final String PARAM_RESTORE_ACCOUNTS = "true";
	private static final String PARAM_NEW_PASSWORD = "secret";
	//(&(cn=\"Akash Nikam\")(sn=Nikam))
	
	//-------------------GET PRINCIPAL PERSON----to be skipped--------------------------
	public void GETPRINCIPALPERSON() throws Exception{
		WSSession session = loginIntoITIM(principal, credential);
		WSPersonService personService = getPersonService();
		WSPerson wsPerson = personService.getPrincipalPerson(session);
		System.out.println("Name :"+wsPerson.getName() +"\n"+wsPerson.getProfileName()+"person DN: "+wsPerson.getItimDN());
		wsPerson.getAttributes();
	}
	
	//-------------------CREATE PERSON---SUCESS--------------------------------------
	public void CREATEPERSON() throws Exception{
		boolean executedSuccessfully = false;
		WSSession session = loginIntoITIM(principal, credential);
		WSPersonService personService = getPersonService();
		
		// Dependency on OrganizationalContainerservice.
		WSOrganizationalContainerService port = getOrganizationalContainerService();
		
		List<WSOrganizationalContainer> lstWSOrgContainers = port.searchContainerByName(
				session, null, "OrganizationalUnit", PARAM_ORG_CONTAINER);
		System.out.println("--------------------");
				if (lstWSOrgContainers != null && !lstWSOrgContainers.isEmpty()){
					System.out.println(lstWSOrgContainers.get(0).toString());
					WSOrganizationalContainer wsContainer = lstWSOrgContainers.get(0);
					//System.out.println(wsContainer.toString());
		
		//----------Adding Attributes--------------------------		
				
		XMLGregorianCalendar date = Utils.long2Gregorian(new Date().getTime());
		
			
		Map<String, Object> map =new HashMap<String, Object>();
		//map.put("objectclass", "erPersonItem");
		map.put("cn", "RahulV");
		map.put("sn", "Vishapayam");
				
		WSPerson wsPerson = createWSPersonAttributes(map);
		boolean isCreatePersonAllowed = personService.isCreatePersonAllowed(session);
		if(isCreatePersonAllowed){
		WSRequest wsRequest = personService.createPerson(session, wsContainer, wsPerson, date);
		Utils.printWSRequestDetails("create person", wsRequest);
		//System.out.println("Create Person"+ wsRequest);
		}else{
			Utils.printMsg(PersonOperations.class.getName(), "execute", null, "The user " + principal + " is not authorized to create a person");
			//System.out.println("Not allowed to create person------");
		}
		executedSuccessfully = true;
	
		}
				if(executedSuccessfully == true){
					WSRequestService requestService=getRequestService();
					System.out.println("Person Created Sucessfully----");
				}
	}
	
	//------------------create Person Method()----------------
	WSPerson createWSPersonAttributes(Map <String, Object> mpParams){
		
		WSPerson wsPerson=new WSPerson();
		wsPerson.setProfileName(ObjectProfileCategory.PERSON);
		System.out.println("*******************");
		java.util.Collection attrList = new ArrayList();
		java.util.Iterator<String> itrParams = mpParams.keySet().iterator();
		WSAttribute wsAttr = null;
	//	WSAttribute wsAttr = new WSAttribute();
		List<WSAttribute> lstWSAttributes = new ArrayList<WSAttribute>();
		while(itrParams.hasNext()){
			String paramName = itrParams.next();
			Object paramValue = mpParams.get(paramName);
			System.out.println("ParamName ="+paramName);
			System.out.println("paramValue ="+paramValue);
			wsAttr = new WSAttribute();
			wsAttr.setName(paramName);
			ArrayOfXsdString arrStringValues = new ArrayOfXsdString();
			
			if(paramValue instanceof String) {
				arrStringValues.getItem().addAll(Arrays.asList((String) paramValue));
			}else if(paramValue instanceof Vector){
				Vector paramValues = (Vector) paramValue;
				arrStringValues.getItem().addAll(paramValues); 
			}else{
				System.out.println(PersonOperations.class.getName()+"The parameter value datatype is not supported.");
			}
			wsAttr.setValues(arrStringValues);
			lstWSAttributes.add(wsAttr);
		}
		ArrayOfTns1WSAttribute attrs = new ArrayOfTns1WSAttribute();
		attrs.getItem().addAll(lstWSAttributes);
		wsPerson.setAttributes(attrs);
		

		return wsPerson;
		
	}
	
	
	//---------------------DELET PERSON METHOD-----------------------------------
	public void DELETEPERSON() throws Exception{
		
		//Search person from root using the cn and sn attribute
		String sFilterParam = PARAM_PERSON_FILTER;
		
		WSSession wsSession = loginIntoITIM(principal, credential);
		WSPersonService personService = getPersonService();
		XMLGregorianCalendar date = Utils.long2Gregorian(new Date().getTime());
		
		List<WSPerson> lstWSPersons = personService.searchPersonsFromRoot(wsSession, sFilterParam, null);
		if(lstWSPersons != null && lstWSPersons.size() > 0){
			WSPerson personToBeDeleted = lstWSPersons.get(0);
			String personDN = personToBeDeleted.getItimDN();
			
			Utils.printMsg(PersonOperations.class.getName(), "execute", "DELETEPERSON", "Deleting the person " + personToBeDeleted.getName() + " having DN " + personToBeDeleted.getItimDN());
		
			//DELETE person method
			WSRequest wsRequest = personService.deletePerson(wsSession, personDN, date);
			Utils.printWSRequestDetails("delete person", wsRequest);
			
		}else{
			Utils.printMsg(PersonOperations.class.getName(), "execute", "DELETEPERSON", "No person found matching the filter : " + sFilterParam);
		}
		
			
	}
	
	
	//---------------------------GET PERSON ROLE--------------------------
	public void GETPERSONROLE() throws WSLoginServiceException, Exception{
		//PARAM_PERSON_FILTER_ROLE = ""
		String sFilterParam = PARAM_PERSON_FILTER_ROLE;
		WSSession wsSession = loginIntoITIM(principal, credential);
		WSPersonService personService = getPersonService();
		//search person from root
		List<WSPerson> lstWSPersons = personService.searchPersonsFromRoot(wsSession, sFilterParam, null);
		if(lstWSPersons != null && lstWSPersons.size() > 0){
			WSPerson person = lstWSPersons.get(0);
			String personDN = person.getItimDN();
			Utils.printMsg(PersonOperations.class.getName(), "execute", null, "Getting roles for user  " + person.getName());
			
			List<WSRole> lstWSRoles = personService.getPersonRoles(wsSession, personDN);
			if(lstWSRoles != null && lstWSRoles.size() > 0){
				Utils.printMsg(PersonOperations.class.getName(), "execute", null, " Number of roles owned by user " + person.getName() + " is " + lstWSRoles.size());
				for(WSRole wsRole : lstWSRoles){
					Utils.printMsg(PersonOperations.class.getName(), "execute", null, "\n Role Name : " + wsRole.getName() +
							"\n Role Description : " + wsRole.getDescription() +
							"\n Role DN : " + wsRole.getItimDN());
					
				}
			}
		}else{
			Utils.printMsg(PersonOperations.class.getName(), "execute", null, "No person found matching the filter : " + sFilterParam);
		}
	}
	
	
	//--------------------LOOKUP PERSON---------------------------------
	public void LOOKUPPERSON() throws WSLoginServiceException, Exception{
		String sFilterParam = PARAM_PERSON_FILTER;
		WSSession wsSession = loginIntoITIM(principal, credential);
		WSPersonService personService = getPersonService();
		List<WSPerson> lstWSPersons = personService.searchPersonsFromRoot(wsSession, sFilterParam, null);
		if(lstWSPersons != null && lstWSPersons.size() > 0){
			//This means we have a person which is to be deleted. If there are more than one person then we select the first one and delete it
			WSPerson person = lstWSPersons.get(0);
			String personDN = person.getItimDN();
			Utils.printMsg(PersonOperations.class.getName(), "execute", null, "Getting roles for user  " + person.getName());
			
			WSPerson wsPerson = personService.lookupPerson(wsSession, personDN);
			Utils.printMsg(PersonOperations.class.getName(), "execute", null, "\n Person Name : " + wsPerson.getName() +
							"\n Profile Name : " + wsPerson.getProfileName() +
							"\n Person DN : " + wsPerson.getItimDN());
					
		}else{
			//Output a message which says that the no person found matching the filter criteria
			Utils.printMsg(PersonOperations.class.getName(), "execute", null, "No person found matching the filter : " + sFilterParam);
			
		}
	}
	//---------------------------GET ACCOUNT BY OWNER-----------------------
	public void GETACCOUNTSBYOWNER() throws WSLoginServiceException, Exception{
		String sFilterParam = PARAM_PERSON_FILTER_ROLE;
		WSSession wsSession = loginIntoITIM(principal, credential);
		WSPersonService personService = getPersonService();
		List<WSPerson> lstWSPersons = personService.searchPersonsFromRoot(wsSession, sFilterParam, null);
		if(lstWSPersons != null && lstWSPersons.size() > 0){
			//This means we have a person which is to be deleted. If there are more than one person then we select the first one and delete it
			WSPerson person = lstWSPersons.get(0);
			String personDN = person.getItimDN();
			Utils.printMsg(PersonOperations.class.getName(), "execute", "GETACCOUNTSBYOWNER", "Getting accounts owned by user  " + person.getName());
			List<WSAccount> lstWSAccounts = personService.getAccountsByOwner(wsSession, personDN);
			if(lstWSAccounts != null && lstWSAccounts.size() > 0){
				Utils.printMsg(PersonOperations.class.getName(), "execute", "GETACCOUNTSBYOWNER", " Number of accounts owned by user " + person.getName() + " is " + lstWSAccounts.size());
				for(WSAccount wsAccount : lstWSAccounts){
					Utils.printMsg(PersonOperations.class.getName(), "execute", "GETACCOUNTSBYOWNER", " \n User ID : " + wsAccount.getName() + " \n" +
							" Service Name : " + wsAccount.getServiceName() + " \n " +
							" Account Profile Name : " + wsAccount.getProfileName() + " \n " +
							" Account DN : " + wsAccount.getItimDN());
				}
			}
			
			
		}else{
			//Output a message which says that the no person found matching the filter criteria
			Utils.printMsg(PersonOperations.class.getName(), "execute", null, "No person found matching the filter : " + sFilterParam);
			
		}
		
	}
	
	//--------------------------MODIFY PERSON-----------------------------
	public void MODIFYPERSON() throws WSLoginServiceException, Exception{
		String sFilterParam = PARAM_PERSON_FILTER_MODIFY;
		String key="mail";
		String value="modify@gmail.com";
		WSSession wsSession = loginIntoITIM(principal, credential);
		WSPersonService personService = getPersonService();
		XMLGregorianCalendar date = Utils.long2Gregorian(new Date().getTime());
		List<WSPerson> lstWSPersons = personService.searchPersonsFromRoot(wsSession, sFilterParam, null);
		if(lstWSPersons != null && lstWSPersons.size() > 0){
			//This means we have a person which is to be modified. 
			//If there are more than one person then we select the first one and modify it
			WSPerson person = lstWSPersons.get(0);
			String personDN = person.getItimDN();	
			Utils.printMsg(PersonOperations.class.getName(), "execute", null, "Modifying person  " + person.getName());
			
			Map<String, Object> map =new HashMap<String, Object>();
			map.put(key, value);
			List<WSAttribute> lstWSAttributes = Utils.getWSAttributesList(map,"MODIFYPERSON");
			if(lstWSAttributes != null && lstWSAttributes.size() > 0){
				WSRequest wsRequest = personService.modifyPerson(wsSession, personDN, lstWSAttributes, date);
				Utils.printWSRequestDetails("modify person", wsRequest);
			}else{
				Utils.printMsg(PersonOperations.class.getName(), "execute", null, "No modify parameters passed to the modifyPerson operation.");
				Utils.printMsg(PersonOperations.class.getName(), "execute", null, "\n" + this.getUsage(""));
			}
		}
	}
	
	//---------------------------ADD ROLE TO PERSON--------------------------------------
	public void ADDROLETOPERSON() throws WSLoginServiceException, Exception{
		String sRoleFilterParam = PARAM_ROLE_FILTER;
		String sPersonFilterParam =PARAM_PERSON_FILTER;
		WSSession wsSession = loginIntoITIM(principal, credential);
		WSPersonService personService = getPersonService();
		
		List<String> roleDNlist = new ArrayList<String>();
		WSRoleService roleService = getRoleService();
		XMLGregorianCalendar date = Utils.long2Gregorian(new Date().getTime());
		List<WSRole> lstWSRoles = roleService.searchRoles(wsSession, sRoleFilterParam);
		if(lstWSRoles != null && lstWSRoles.size() > 0){
			Utils.printMsg(PersonOperations.class.getName(), "execute", "ADDROLESTOPERSON", "Role(s) matching the filter, "+ sRoleFilterParam+ ", are: ");
			for (Iterator<WSRole> iter = lstWSRoles.iterator(); iter.hasNext();) {
				WSRole wsRole = (WSRole) iter.next();
				String roleDN = wsRole.getItimDN();
				roleDNlist.add(roleDN);
				//Utils.printMsg(WSRoleServiceClient.class.getName(), "execute", "ADDROLESTOPERSON", roleDN);
			}
		}else{
			Utils.printMsg(PersonOperations.class.getName(), "execute", "ADDROLESTOPERSON", "No role found matching the filter : " + sRoleFilterParam);
			
		}
		
		List<WSPerson> lstWSPersons = personService.searchPersonsFromRoot(wsSession, sPersonFilterParam, null);
		if(lstWSPersons != null && lstWSPersons.size() > 0){
			//This means the requested person exists. If there are more than one person then we select the first one.
			WSPerson person = lstWSPersons.get(0);
			String personDN = person.getItimDN();
			Utils.printMsg(PersonOperations.class.getName(), "execute", "ADDROLESTOPERSON", "Adding role(s) to a person, " + person.getName() + ", having DN " + person.getItimDN());

			WSRequest wsRequest = personService.addRolesToPerson(wsSession, personDN, roleDNlist, date);
			Utils.printWSRequestDetails("add roles to person", wsRequest);
			System.out.println("Add role to person request submitted successfully. Request ID: "+ wsRequest.getRequestId());
			
		}else{
			//Output a message which says that the no person found matching the filter criteria
			Utils.printMsg(PersonOperations.class.getName(), "execute", "ADDROLESTOPERSON", "No person found matching the filter : " + sPersonFilterParam);
			
		}
		
	}
	
	//--------------------------REMOVE ROLES FROM PERSON----------------------------------
	public void REMOVEROLESFROMPERSON() throws WSLoginServiceException, Exception{
		String sRoleFilterParam = PARAM_ROLE_FILTER;
		String sPersonFilterParam =PARAM_PERSON_FILTER;
		WSSession wsSession = loginIntoITIM(principal, credential);
		WSPersonService personService = getPersonService();
		XMLGregorianCalendar date = Utils.long2Gregorian(new Date().getTime());
	
		List<String> roleDNlist = new ArrayList<String>();
		WSRoleService roleService = getRoleService();
		List<WSRole> lstWSRoles = roleService.searchRoles(wsSession, sRoleFilterParam);
		if(lstWSRoles != null && lstWSRoles.size() > 0){
			Utils.printMsg(PersonOperations.class.getName(), "execute", "REMOVEROLESFROMPERSON", "Role(s) matching the filter, "+ sRoleFilterParam+ ", are: ");
			for (Iterator<WSRole> iter = lstWSRoles.iterator(); iter.hasNext();) {
				WSRole wsRole = (WSRole) iter.next();
				String roleDN = wsRole.getItimDN();
				roleDNlist.add(roleDN);
			//	Utils.printMsg(WSRoleServiceClient.class.getName(), "execute", "REMOVEROLESFROMPERSON", roleDN);
			}
		}else{
			Utils.printMsg(PersonOperations.class.getName(), "execute", "REMOVEROLESFROMPERSON", "No role found matching the filter : " + sRoleFilterParam);
			
		}
		
		List<WSPerson> lstWSPersons = personService.searchPersonsFromRoot(wsSession, sPersonFilterParam, null);
		if(lstWSPersons != null && lstWSPersons.size() > 0){
			//This means the requested person exists. If there are more than one person then we select the first one.
			WSPerson person = lstWSPersons.get(0);
			String personDN = person.getItimDN();
			Utils.printMsg(PersonOperations.class.getName(), "execute", "REMOVEROLESFROMPERSON", "Removing role(s) from a person, " + person.getName() + ", having DN " + person.getItimDN());

			WSRequest wsRequest = personService.removeRolesFromPerson(wsSession, personDN, roleDNlist, date);
			Utils.printWSRequestDetails("remove roles from person", wsRequest);
			System.out.println("Remove roles from person request submitted successfully. Request ID: "+ wsRequest.getRequestId());
			
		}else{
			//Output a message which says that the no person found matching the filter criteria
			Utils.printMsg(PersonOperations.class.getName(), "execute", "REMOVEROLESFROMPERSON", "No person found matching the filter : " + sPersonFilterParam);
			
		}
		
	}
	
	//---------------------------SUSPEND PERSON-----------------------------
	public void SUSPENDPERSONADVANCED() throws WSLoginServiceException, Exception{
		String sFilterParam = PARAM_PERSON_FILTER;
		WSSession wsSession = loginIntoITIM(principal, credential);
		WSPersonService personService = getPersonService();
		
		List<WSPerson> lstWSPersons = personService.searchPersonsFromRoot(wsSession, sFilterParam, null);
		if(lstWSPersons != null && lstWSPersons.size() > 0){
			//This means we have a person which is to be deleted. If there are more than one person then we select the first one and delete it
			WSPerson person = lstWSPersons.get(0);
			String personDN = person.getItimDN();
			Utils.printMsg(PersonOperations.class.getName(), "execute", null, "Suspending person  " + person.getName());
			
			boolean includeAccounts = true;
			//boolean includeAccounts = Boolean.getBoolean((String)mpParams.get(PARAM_INCLUDE_ACCOUNTS));
			String sDate = PARAM_DATE;
			Date date = null;
			if(sDate != null){
				try{
					date = Utils.convertStringToDate(sDate);
				}catch(ParseException e){
					Utils.printMsg(PersonOperations.class.getName(), "execute", null, "The date is not specified in the expected format. Expected format is MM/DD/YYYY");
					Utils.printMsg(PersonOperations.class.getName(), "execute",null, this.getUsage(e.getLocalizedMessage()));
				}
			}else{
				date = new Date();
			}
			
			
			
			XMLGregorianCalendar xmlDate = Utils.long2Gregorian(date.getTime());
			WSRequest wsRequest = personService.suspendPersonAdvanced(wsSession, personDN, includeAccounts, xmlDate);
			Utils.printWSRequestDetails("Suspended person ", wsRequest);
			
			
			
		}else{
			//Output a message which says that the no person found matching the filter criteria
			Utils.printMsg(PersonOperations.class.getName(), "execute", null, "No person found matching the filter : " + sFilterParam);
			
		}
		
		
	}
	
	//--------------------------RESTORE PERSON-----------------------------
	public void RESTOREPERSON() throws WSLoginServiceException, Exception{
		String sFilterParam = PARAM_PERSON_FILTER;
		WSSession wsSession = loginIntoITIM(principal, credential);
		WSPersonService personService = getPersonService();
		XMLGregorianCalendar date = Utils.long2Gregorian(new Date().getTime());
		
		List<WSPerson> lstWSPersons = personService.searchPersonsFromRoot(wsSession, sFilterParam, null);
		if(lstWSPersons != null && lstWSPersons.size() > 0){
			//This means we have a person which is to be deleted. If there are more than one person then we select the first one and delete it
			WSPerson person = lstWSPersons.get(0);
			String personDN = person.getItimDN();
			Utils.printMsg(PersonOperations.class.getName(), "execute", null, "Restoring user  " + person.getName());
			String sRestoreAccts = PARAM_RESTORE_ACCOUNTS;
			boolean restoreAccounts = new Boolean(sRestoreAccts);
			String password = PARAM_NEW_PASSWORD;
			
			WSRequest wsRequest = personService.restorePerson(wsSession, personDN, restoreAccounts, password, date);
			Utils.printWSRequestDetails("restore person", wsRequest);
						
			
		}else{
			//Output a message which says that the no person found matching the filter criteria
			Utils.printMsg(PersonOperations.class.getName(), "execute", null, "No person found matching the filter : " + sFilterParam);
			
		}
		
	}
	//---------------------------LOGIN ITIM--------------------------------
	private static WSSession loginIntoITIM(String principal2, String credential) throws Exception, WSLoginServiceException {
		WSSessionService proxy = getSessionService();
		WSSession session = proxy.login(principal, credential);
		return session;
	}
	
	@Override
	public boolean executeOperation(Map<String, Object> mpParams) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getUsage(String errMessage) {
		// TODO Auto-generated method stub
		return null;
	}

	
	public static void main(String args[]) throws Exception{
		PersonOperations personOperation = new PersonOperations();
		BufferedReader object = new BufferedReader (new InputStreamReader(System.in));
		System.out.println("Select the option from the list you want to perform as:"
				+ "\n1. GETPRINCIPALPERSON"
				+ "\n2. CREATEPERSON "
				+ "\n3. DELETEPERSON"
				+ "\n4. GETPERSONROLE"
				+ "\n5. LOOKUPPERSON"
				+ "\n6. GETACCOUNTSBYOWNER"
				+ "\n7. MODIFYPERSON"
				+ "\n8. ADDROLETOPERSON"
				+ "\n9. REMOVEROLESFROMPERSON"
				+ "\n10. SUSPENDPERSONADVANCED"
				+ "\n11. RESTOREPERSON\n");
		int choice= Integer.parseInt(object.readLine());
		try{
			//ModificationItem[] modItem=new ModificationItem[1];
			switch(choice){
			case 1:
				personOperation.GETPRINCIPALPERSON();
				break;
			case 2:
				personOperation.CREATEPERSON();
				break;
			case 3:
				personOperation.DELETEPERSON();
				break;
			case 4:
				personOperation.GETPERSONROLE();
				break;				
			case 5:
				personOperation.LOOKUPPERSON();
				break;
			case 6:
				personOperation.GETACCOUNTSBYOWNER();
				break;
			case 7:
				personOperation.MODIFYPERSON();
				break;
			case 8:
				personOperation.ADDROLETOPERSON();
				break;
			case 9:
				personOperation.REMOVEROLESFROMPERSON();
				break;
			case 10:
				personOperation.SUSPENDPERSONADVANCED();
				break;
			case 11:
				personOperation.RESTOREPERSON();
				break;
				default:
					System.out.println("Invalid Entry!");
				break;
			}
			
		}
			catch(NumberFormatException e){
				  System.out.println(e.getMessage() + " is not a numeric value.");
				  //System.exit(0);
	
						
			}
	}
}

	