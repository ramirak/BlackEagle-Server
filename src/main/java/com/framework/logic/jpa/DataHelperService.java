package com.framework.logic.jpa;

import org.springframework.stereotype.Service;

import com.framework.boundaries.DataBoundary;
import com.framework.constants.CommandType;
import com.framework.constants.DataKeyValue;
import com.framework.constants.UserData;
import com.framework.constants.UserRole;
import com.framework.data.UserEntity;
import com.framework.exceptions.BadRequestException;
import com.framework.exceptions.UnauthorizedRequest;

@Service
public class DataHelperService {

	public Double getFileSize(long size) {
		return ((double) size / 1048576);
	}

	public void notAllowedTypes(String type, UserData[] types) {
		for (int i = 0; i < types.length; i++) {
			if (type.equals(types[i].name()))
				throw new BadRequestException("Call to wrong method");
		}
		return;
	}

	public void allowedTypes(String type, UserData[] types) {
		for (int i = 0; i < types.length; i++) {
			if (type.equals(types[i].name()))
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

	public String CheckDomain(String domain) {
		domain = domain.replaceAll("\\s+", "");
		if (!domain.matches(
				"^((?!-))(xn--)?[a-z0-9][a-z0-9-_]{0,61}[a-z0-9]{0,1}\\.(xn--)?([a-z0-9\\-]{1,61}|[a-z0-9-]{1,30}\\.[a-z]{2,})$"))
			throw new BadRequestException("invalid domain name");
		return domain;
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

	private void requireCommand(String cmd) {
		if (cmd.equals(CommandType.taskkill) || cmd.equals(CommandType.tasklist) || cmd.equals(CommandType.dirHidden)
				|| cmd.equals(CommandType.dirOrdered))
			return;
		throw new BadRequestException("Invalid command type");
	}

	private void requireParam(String cmd, Object param) {
		if (!cmd.equals(CommandType.tasklist)) {
			if (!param.toString().matches("([\\\\a-zA-Z0-9_.-])+"))
				throw new BadRequestException("Additional parameter should be a path or filename");
		} else if (!param.toString().isBlank()) {
			throw new BadRequestException("This command cannot have additional parameters");
		}
	}
}
