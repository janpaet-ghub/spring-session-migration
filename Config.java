import de.fms.scm.web.session.CustomMapSessionRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.session.MapSessionRepository;
import org.springframework.session.config.annotation.web.http.EnableSpringHttpSession;
import org.springframework.session.web.context.AbstractHttpSessionApplicationInitializer;

import java.util.concurrent.ConcurrentHashMap;

@Configuration
@EnableSpringHttpSession
public class Config
        extends AbstractHttpSessionApplicationInitializer {

    @Bean
    public MapSessionRepository sessionRepository() {
        return new CustomMapSessionRepository(new ConcurrentHashMap<>());
    }
}
