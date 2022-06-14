package com.framework.logic.jpa;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.framework.boundaries.EventBoundary;
import com.framework.constants.DataKeyValue;
import com.framework.constants.EventType;
import com.framework.data.EventEntity;
import com.framework.data.UserEntity;
import com.framework.data.dao.EventDao;
import com.framework.data.dao.UserDao;
import com.framework.exceptions.NotFoundException;
import com.framework.logic.EventService;
import com.framework.logic.converters.EventEntityConverterImplementation;
import com.framework.logic.converters.JsonConverterImplementation;
import com.framework.security.sessions.SessionAttributes;

@Service
public class EventServiceJpa implements EventService {
	private EventDao eventDao;
	private UserDao userDao;
	private JsonConverterImplementation jsonConverter;
	private EventEntityConverterImplementation evConverter;
	private SessionAttributes session;

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
	public void setEvConverter(EventEntityConverterImplementation evConverter) {
		this.evConverter = evConverter;
	}

	@Autowired
	public void setSession(SessionAttributes session) {
		this.session = session;
	}

	@Override
	@Transactional
	public EventEntity createEvent(String creator, EventType eventType) {
		Map<String, Object> eventAttr = new TreeMap<>();
		// Map the IP address to a new attribute
		eventAttr.put(DataKeyValue.IP_ADDR.name(), session.retrieveIpAddress());

		EventEntity newEvent = new EventEntity(eventType.name(), new Date(), jsonConverter.mapToJSON(eventAttr));
		// Find the corresponding user in the database
		UserEntity existingEntity = userDao.findById(creator)
				.orElseThrow(() -> new NotFoundException("User not found: "));

		existingEntity.addEventToUser(newEvent);
		eventDao.save(newEvent);
		return newEvent;
	}

	@Override
	@Transactional
	public List<EventBoundary> getAllData(int page, int size) {
		String authenticatedUser = session.retrieveAuthenticatedUsername();

		return eventDao
				.findAllByEventOwnerUid(authenticatedUser, PageRequest.of(page, size, Direction.DESC, "timeOfEvent"))
				.stream().map(this.evConverter::toBoundary).collect(Collectors.toList());
	}
}
