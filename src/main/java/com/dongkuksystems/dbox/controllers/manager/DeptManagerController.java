package com.dongkuksystems.dbox.controllers.manager;

import com.dongkuksystems.dbox.controllers.AbstractCommonController;
import com.dongkuksystems.dbox.models.api.response.ApiResult;
import com.dongkuksystems.dbox.models.dto.type.manager.deptmanager.DeptManagerDto;
import com.dongkuksystems.dbox.services.manager.deptmanager.DeptManagerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import static com.dongkuksystems.dbox.models.api.response.ApiResult.OK;

@RestController
@RequestMapping("/api")
public class DeptManagerController extends AbstractCommonController {

	@Autowired
	private DeptManagerService deptManagerService;

	// TODO Refactoring. Move this method.
	@GetMapping("/dbox-users/positions")
	public ApiResult<List<Map<String, Object>>> selectPositions() {
		return OK(deptManagerService.selectPositions());
	}

	@GetMapping("/dept-managers")
	public ApiResult<List<Map<String, Object>>> selectDeptManagers(DeptManagerDto deptManagerDto) {
		return OK(deptManagerService.selectDeptManagers(deptManagerDto));
	}

	@DeleteMapping("/dept-managers/{objectId}")
	public ApiResult<Boolean> deleteDeptManager(@PathVariable String objectId) {
		deptManagerService.deleteDeptManager(objectId);
		return OK(true);
	}

}
