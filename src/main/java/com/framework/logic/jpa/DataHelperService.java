package com.framework.logic.jpa;

import java.nio.file.InvalidPathException;

import org.springframework.stereotype.Service;

import com.framework.boundaries.DataBoundary;
import com.framework.constants.CommandType;
import com.framework.constants.ConfigType;
import com.framework.constants.DataKeyValue;
import com.framework.constants.FilterType;
import com.framework.constants.UserData;
import com.framework.constants.UserRole;
import com.framework.data.UserEntity;
import com.framework.exceptions.BadRequestException;
import com.framework.exceptions.UnauthorizedRequest;

@Service
public class DataHelperService {

	public void notAllowedTypes(DataBoundary newData, UserData[] types) {
		for (int i = 0; i < types.length; i++) {
			if (newData.getDataType() == types[i])
				throw new BadRequestException("Call to wrong method");
		}
		return;
	}

	public void allowedTypes(DataBoundary newData, UserData[] types) {
		for (int i = 0; i < types.length; i++) {
			if (newData.getDataType() == types[i])
				return;
		}
		throw new BadRequestException("Call to wrong method");
	}

	public void checkDataType(String type) {
		if (type == null)
			throw new BadRequestException("Invalid request type");

		for (UserData ud : UserData.values()) {
			if (ud.name().equals(type)) {
				return;
			}
		}
		throw new BadRequestException("Invalid request type");
	}

	public void checkRequest(UserEntity existingOwner, DataBoundary newData) {
		// Check if the owner is a device
		if (!existingOwner.getRole().equals(UserRole.DEVICE.name()))
			throw new UnauthorizedRequest("Only devices can have Data of type Request");
		// Check if the request type is valid

		Object requestType = newData.getDataAttributes().get(DataKeyValue.REQUEST_TYPE.name());
	
		if (requestType != null)
			checkDataType(requestType.toString());
		if (requestType.toString().equals(UserData.COMMAND.name())) {
			Object cmd = newData.getDataAttributes().get(DataKeyValue.COMMAND_TYPE.name());
			requireCommand(cmd.toString());

			Object cmdParam = newData.getDataAttributes().get(DataKeyValue.COMMAND_PARAMETER.name());
			requireParam(cmd.toString(), cmdParam);
		}
	}

	public void checkConfig(UserEntity existingOwner, DataBoundary newData) {
		if (existingOwner.getRole().equals(UserRole.DEVICE.name())) {
			Object filterA = newData.getDataAttributes().get(FilterType.FAKENEWS.name());
			Object filterB = newData.getDataAttributes().get(FilterType.GAMBLING.name());
			Object filterC = newData.getDataAttributes().get(FilterType.PORN.name());
			Object filterD = newData.getDataAttributes().get(FilterType.SOCIAL.name());

			if (filterA == null || !(filterA instanceof String) || filterB == null || !(filterB instanceof String)
					|| filterC == null || !(filterC instanceof String) || filterD == null
					|| !(filterD instanceof String))
				throw new BadRequestException("Invalid filter type");

		} else { // User of either type PLAYER or ADMIN
			Object optA = newData.getDataAttributes().get(ConfigType.NOTIFICATION_EMAIL.name());
			Object optB = newData.getDataAttributes().get(ConfigType.DELETE_IF_INACTIVE.name());
			if (optA == null || !(optA instanceof String) || optB == null || !(optB instanceof String))
				throw new BadRequestException("Invalid config type");
		}
	}

	private void requireCommand(String cmd) {
		if (cmd.equals(CommandType.taskkill) ||
			cmd.equals(CommandType.tasklist) ||
			cmd.equals(CommandType.dirHidden) ||
			cmd.equals(CommandType.dirOrdered))
				return;
		throw new BadRequestException("Invalid command type");
	}

	private void requireParam(String cmd, Object param) {
		if (param == null)
			if (cmd.equals(CommandType.taskkill) || cmd.equals(CommandType.dirHidden)
					|| cmd.equals(CommandType.dirOrdered)) {
				throw new BadRequestException("This command requires additional parameter");
			}
		if(!param.toString().matches("([\\\\a-zA-Z0-9_.-])+")) {
			throw new BadRequestException("Additional parameter should be a path or filename");
		}
	}

}
