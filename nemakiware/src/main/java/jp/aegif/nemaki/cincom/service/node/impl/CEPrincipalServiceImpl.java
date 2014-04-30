package jp.aegif.nemaki.cincom.service.node.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jp.aegif.nemaki.cincom.util.CincomManager;
import jp.aegif.nemaki.cincom.util.constant.CincomPropertyKey;
import jp.aegif.nemaki.model.Group;
import jp.aegif.nemaki.model.User;
import jp.aegif.nemaki.util.constant.NodeType;
import jp.aegif.nemaki.service.node.PrincipalService;
import jp.aegif.nemaki.util.NemakiPropertyManager;
import jp.aegif.nemaki.util.PasswordHasher;

import com.cincom.kmdata.client.api.user.AccessDeniedException_Exception;
import com.cincom.kmdata.client.api.user.ItemNotFoundException_Exception;
import com.cincom.kmdata.client.api.user.RepositoryException_Exception;
import com.cincom.kmdata.client.api.user.UserWS;

public class CEPrincipalServiceImpl implements PrincipalService{

	private CincomManager cincomManager;
	private UserWS userWS;
	private NemakiPropertyManager propertyManager;
	
	public void init(){
		userWS = cincomManager.getUserWS();
	}
	
	@Override
	public List<User> getUsers() {
		try {
			
			try {
				userWS.getGrantedGroupsList(cincomManager.getContext(), "/Sites/ggg");
			} catch (AccessDeniedException_Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ItemNotFoundException_Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
			
			List<User> users = new ArrayList<User>();
			List<String> userNames = userWS.getUsers(cincomManager.getContext());
			
			for(String userName : userNames){
				User user = getUserById(userName);
				users.add(user);
			}
			return users;
			
		} catch (RepositoryException_Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
		
	}

	@Override
	public List<Group> getGroups() {
	
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<String> getGroupIdsContainingUser(String username) {
		try {
			List<String> _groups = userWS.getGroupsOfUsers(cincomManager.getContext(), username);
			Set<String> groups = new HashSet<String>(_groups);
			return groups;
		} catch (RepositoryException_Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public User getUserById(String userId) {
		com.cincom.kmdata.client.api.user.User ceUser;
		try {
			ceUser = userWS.getUserAttributes(cincomManager.getContext(), userId, null);
			User user = new User(ceUser.getName(), ceUser.getLongName(), ceUser.getName(), "", ceUser.getEmail(), PasswordHasher.hash(ceUser.getPassword()));
			user.setType(NodeType.USER.value());
			return user;
		} catch (RepositoryException_Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public Group getGroupById(String groupId) {
		try {
			com.cincom.kmdata.client.api.user.Group ceGroup  = userWS.getGroup(cincomManager.getContext(), groupId);
			Group group = new Group();
			group.setType(NodeType.GROUP.value());
			group.setId(ceGroup.getName());
			
		} catch (ItemNotFoundException_Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RepositoryException_Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public void createUser(User user) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateUser(User user) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void deleteUser(String userId) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void createGroup(Group group) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateGroup(Group group) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void deleteGroup(String groupId) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public User getAdmin() {
		String adminId = propertyManager.readValue(CincomPropertyKey.CINCOM_PRINCIPAL_ADMIN_ID);
		return getUserById(adminId);
	}

	@Override
	public String getAnonymous() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getAnyone() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setCincomManager(CincomManager cincomManager) {
		this.cincomManager = cincomManager;
	}

	public void setPropertyManager(NemakiPropertyManager propertyManager) {
		this.propertyManager = propertyManager;
	}
}
