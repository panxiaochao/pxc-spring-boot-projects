package io.github.test.service;

import com.aliyun.dingtalkoauth2_1_0.Client;
import com.aliyun.dingtalkoauth2_1_0.models.GetAccessTokenResponse;
import com.aliyun.dingtalkoauth2_1_0.models.GetCorpAccessTokenRequest;
import com.aliyun.dingtalkoauth2_1_0.models.GetCorpAccessTokenResponse;
import com.aliyun.teaopenapi.models.Config;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 钉钉 服务接口
 * </p>
 *
 * @author lypxc
 * @since 2025-09-01
 * @version 1.0
 */
@Service
@Slf4j
public class DDService {

	/**
	 * 使用 Token 初始化账号Client
	 * @return Client
	 */
	private Client createClient() throws Exception {
		Config config = new Config();
		config.protocol = "https";
		config.regionId = "central";
		return new Client(config);
	}

	public GetCorpAccessTokenResponse getAccessToken() throws Exception {
		Client client = createClient();
		GetCorpAccessTokenRequest getCorpAccessTokenRequest = new GetCorpAccessTokenRequest()
			.setSuiteKey("suiteuttgfitcuhxbie9w")
			.setSuiteSecret("a_vysuuDTy24LPv3_3VuZLxUm5CITC91IwsnureBT-13PDFr89x_BrM7nf7M94r-")
			.setAuthCorpId("39004001")
			.setSuiteTicket("iT9fK1kN3yG9nQ9cP3gA3yW7eJ2dJ6fU");
		try {
			return client.getCorpAccessToken(getCorpAccessTokenRequest);
		}
		catch (Exception _err) {
			throw new RuntimeException(_err);

		}
	}

}
