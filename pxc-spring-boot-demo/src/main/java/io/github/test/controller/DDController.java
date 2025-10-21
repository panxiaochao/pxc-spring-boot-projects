package io.github.test.controller;

import com.aliyun.dingtalkoauth2_1_0.models.GetCorpAccessTokenResponse;
import io.github.test.service.DDService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * 钉钉控制器
 * </p>
 *
 * @author lypxc
 * @since 2025-09-01
 * @version 1.0
 */
@RestController
@RequestMapping("/dd")
@RequiredArgsConstructor
public class DDController {

	private final DDService ddService;

	@GetMapping("/getAccessToken")
	public GetCorpAccessTokenResponse getAccessToken() throws Exception {
		return ddService.getAccessToken();
	}

}
