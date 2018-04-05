package de.adorsys.aspsp.aspspmockserver;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@Profile("fongo")
public class AspspMockServerApplicationTests {

	@Test
	public void contextLoads() {
	}

}
