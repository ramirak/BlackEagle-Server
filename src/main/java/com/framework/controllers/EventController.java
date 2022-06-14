package com.framework.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.framework.boundaries.EventBoundary;
import com.framework.logic.EventService;

@RestController
public class EventController {
	private EventService eventService;

	@Autowired
	public void setEventService(EventService eventService) {
		this.eventService = eventService;
	}

	@RequestMapping(path = "/events/getAll", method = RequestMethod.GET)
	public EventBoundary[] getAllData(@RequestParam(name = "page", required = false, defaultValue = "0") int page,
			@RequestParam(name = "size", required = false, defaultValue = "5") int size) {
		return eventService.getAllData(page, size).toArray(new EventBoundary[0]);
	}
}
