package com.framework.logic.jpa;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.framework.data.EventEntity;
import com.framework.data.UserEntity;
import com.framework.data.dao.EventDao;
import com.framework.data.dao.UserDao;
import com.framework.exceptions.NotFoundException;
import com.framework.logic.EventService;

@Service
public class EventServiceJpa implements EventService{
	private EventDao eventDao;
	private UserDao userDao;
	
	@Autowired
	public void setEventDao(EventDao eventDao) {
		this.eventDao = eventDao;
	}
	
	@Autowired
	public void setUserDao(UserDao userDao) {
		this.userDao = userDao;
	}
	
	@Override
	public EventEntity createEvent(String creator,EventEntity event) {
		
		// Find the corresponding user in the database
		UserEntity existingEntity = userDao.findById(creator)
				.orElseThrow(() -> new NotFoundException("User not found: "));
		
		existingEntity.addEventToUser(event);
		userDao.save(existingEntity);
		return event;
	}

}
