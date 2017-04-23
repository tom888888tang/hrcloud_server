/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package odata.service.user.data;

import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.ejb.EJB;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.PersistenceContext;
import javax.sql.DataSource;

import odata.service.user.service.EdmProvider;
import odata.service.util.Util;
import persistence.User;
import persistence.UserBean;

import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.EntityCollection;
import org.apache.olingo.commons.api.data.Property;
import org.apache.olingo.commons.api.data.ValueType;
import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.commons.api.ex.ODataRuntimeException;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriParameter;
import org.eclipse.persistence.config.PersistenceUnitProperties;


public class Storage {
    @PersistenceContext
    private EntityManager em;
    private static final String PERSISTENCE_UNIT_NAME = "cloudhr_server";  
    private static EntityManagerFactory factory;  
	private List<Entity> userlist;
    @EJB
	private UserBean userBean;
    private DataSource ds;
    private EntityManagerFactory emf;
	
	public Storage(String user_id) {
		userlist = new ArrayList<Entity>();
		initData(user_id);
	}

	/* PUBLIC FACADE */

	public EntityCollection readEntitySetData(EdmEntitySet edmEntitySet)throws ODataApplicationException{

		// actually, this is only required if we have more than one Entity Sets
		if(edmEntitySet.getName().equals(EdmProvider.ES_USERS_NAME)){
			return getUsers();
		}

		return null;
	}

	public Entity readEntityData(EdmEntitySet edmEntitySet, List<UriParameter> keyParams) throws ODataApplicationException{

		EdmEntityType edmEntityType = edmEntitySet.getEntityType();

		// actually, this is only required if we have more than one Entity Type
		if(edmEntityType.getName().equals(EdmProvider.ET_USER_NAME)){
			return getUser(edmEntityType, keyParams);
		}

		return null;
	}



	/*  INTERNAL */

	private EntityCollection getUsers(){
		EntityCollection retEntitySet = new EntityCollection();

		for(Entity userEntity : this.userlist){
			   retEntitySet.getEntities().add(userEntity);
		}

		return retEntitySet;
	}


	private Entity getUser(EdmEntityType edmEntityType, List<UriParameter> keyParams) throws ODataApplicationException{

		// the list of entities at runtime
		EntityCollection entitySet = getUsers();
		
		/*  generic approach  to find the requested entity */
		Entity requestedEntity = Util.findEntity(edmEntityType, entitySet, keyParams);
		
		if(requestedEntity == null){
			// this variable is null if our data doesn't contain an entity for the requested key
			// Throw suitable exception
			throw new ODataApplicationException("Entity for requested key doesn't exist",
          HttpStatusCode.NOT_FOUND.getStatusCode(), Locale.ENGLISH);
		}

		return requestedEntity;
	}

	/* HELPER */

	public void initData(String user_id){
		Connection connection = null;
        try {
            InitialContext ctx = new InitialContext();
            ds = (DataSource) ctx.lookup("java:comp/env/jdbc/DefaultDB");

            Map properties = new HashMap();
            properties.put(PersistenceUnitProperties.NON_JTA_DATASOURCE, ds);
            emf = Persistence.createEntityManagerFactory("cloudhr_server", properties);
            //emf = Persistence.createEntityManagerFactory("cloudhr_server");
        } catch (NamingException e) {
            e.printStackTrace();
        }
 
	        EntityManager em = emf.createEntityManager();  
	        //List<User> UserList = userBean.getAllUsers();
		    String query = "select u from User u where u.user_id = '" + user_id + "' "; 
	        List<User> UserList = em.createQuery(query).getResultList();
	        //List<User> UserList = em.createNamedQuery("AllUsers", User.class).getResultList();
	        for(int i = 0;i < UserList.size(); i ++){
	      	  Entity et = new Entity();
	      	  et.addProperty(new Property(null, "User_id", ValueType.PRIMITIVE, UserList.get(i).getUser_id() ))
	  		  .addProperty(new Property(null, "Status", ValueType.PRIMITIVE, UserList.get(i).getStatus()))
	  		  .addProperty(new Property(null, "Last_name", ValueType.PRIMITIVE, UserList.get(i).getLastname()))
	  		  .addProperty(new Property(null, "First_name", ValueType.PRIMITIVE, UserList.get(i).getFirstname()))
	  		  .addProperty(new Property(null, "Mobile", ValueType.PRIMITIVE, UserList.get(i).getMobile()))
	  		  .addProperty(new Property(null, "Wechat_id", ValueType.PRIMITIVE, UserList.get(i).getWechat_id()))
	  		  .addProperty(new Property(null, "Sf_learning_user", ValueType.PRIMITIVE, UserList.get(i).getSf_learning_user()))
	  		  .addProperty(new Property(null, "Sf_learning_user_type", ValueType.PRIMITIVE, UserList.get(i).getSf_learning_user_type()))
	  		  ;
	      	  et.setId(createId("Users", UserList.get(i).getUser_id()));
	  		  
	      	userlist.add(et);
	        }
	        
/*	      	  Entity et = new Entity();
	      	  et.addProperty(new Property(null, "User_id", ValueType.PRIMITIVE, '1' ))
	  		  .addProperty(new Property(null, "Status", ValueType.PRIMITIVE, '1'))
	  		  .addProperty(new Property(null, "Last_name", ValueType.PRIMITIVE, '1'))
	  		  .addProperty(new Property(null, "First_name", ValueType.PRIMITIVE, '1'))
	  		  .addProperty(new Property(null, "Mobile", ValueType.PRIMITIVE, '1'))
	  		  .addProperty(new Property(null, "Wechat_id", ValueType.PRIMITIVE, '1'))
	  		  .addProperty(new Property(null, "Sf_user", ValueType.PRIMITIVE, '1'))
	  		  ;
	      	  et.setId(createId("Users", 1));
	  		  
	      	userlist.add(et);*/
	      
		
	}

	private URI createId(String entitySetName, Object id) {
		try {
			return new URI(entitySetName + "(" + String.valueOf(id) + ")");
		} catch (URISyntaxException e) {
			throw new ODataRuntimeException("Unable to create id for entity: " + entitySetName, e);
		}
	}
}
