package io.github.test.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * </p>
 *
 * @author lypxc
 * @since 2025-10-21
 * @version 1.0
 */
@RestController
@RequestMapping
public class StaticController {

	@GetMapping("/favicon.ico")
	public byte[] favicon() {
		// 返回空字节数组或重定向到其他资源
		return new byte[0];
	}

}
