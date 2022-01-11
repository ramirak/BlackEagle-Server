package com.framework.logic.jpa;

import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.stereotype.Service;

import com.framework.constants.EventKey;
import com.framework.constants.EventType;
import com.framework.data.EventEntity;
import com.framework.data.UserEntity;
import com.framework.data.dao.EventDao;
import com.framework.data.dao.UserDao;
import com.framework.exceptions.NotFoundException;
import com.framework.logic.EventService;
import com.framework.logic.converters.JsonConverter;
import com.framework.logic.converters.JsonConverterImplementation;
import com.framework.security.sessions.SessionAttributes;

@Service
public class EventServiceJpa implements EventService{
	private EventDao eventDao;
	private UserDao userDao;
	private JsonConverterImplementation jsonConverter;
	private SessionAttributes sessionAttr;
	
	@Autowired
	public void setEventDao(EventDao eventDao) {
		this.eventDao = eventDao;
	}
	
	@Autowired
	public void setUserDao(UserDao userDao) {
		this.userDao = userDao;
	}
	
	@Autowired
	public void setJsonConverter(JsonConverterImplementation jsonConverter) {
		this.jsonConverter = jsonConverter;
	}
	
	@Autowired
	public void setSessionAttr(SessionAttributes sessionAttr) {
		this.sessionAttr = sessionAttr;
	}
	
	@Override
	public EventEntity createEvent(String creator,EventType eventType) {
	    Map<String, Object> eventAttr = new TreeMap<>();
	    // Map the IP address to a new attribute
	    eventAttr.put(EventKey.IP_ADDR.name(), sessionAttr.retrieveIpAddress());
	    
		EventEntity newEvent = new EventEntity(eventType.name(), new Date(), jsonConverter.mapToJSON(eventAttr));
		// Find the corresponding user in the database
		UserEntity existingEntity = userDao.findById(creator)
				.orElseThrow(() -> new NotFoundException("User not found: "));
		
		existingEntity.addEventToUser(newEvent);
		eventDao.save(newEvent);
		return newEvent;
	}

}
