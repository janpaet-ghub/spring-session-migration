package de.fms.scm.web.session;

import org.apache.commons.lang.StringUtils;
import org.springframework.session.MapSession;
import org.springframework.session.MapSessionRepository;
import org.springframework.session.Session;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

public class CustomMapSessionRepository extends MapSessionRepository {

    private Integer defaultMaxInactiveInterval;

    private final Map<Long, String> threadSessions = new HashMap<>();

    /**
     * Creates a new instance backed by the provided {@link Map}. This allows
     * injecting a distributed {@link Map}.
     *
     * @param sessions the {@link Map} to use. Cannot be null.
     */
    public CustomMapSessionRepository(Map<String, Session> sessions) {
        super(sessions);
    }

    @Override
    public MapSession createSession() {
        //TODO SCM-1302 Login Performance, Warum ist dies hier?
        long threadId = Thread.currentThread().getId();
        String sessionId = threadSessions.get(threadId);
        MapSession result = null;
        if (StringUtils.isNotEmpty(sessionId)){
            result = findById(sessionId);
            threadSessions.remove(threadId);
        }
        if (result == null){
            result = new MapSession();
        }

        //TODO SCM-1302 Login Performance, Warum ist dies hier?
        if (this.defaultMaxInactiveInterval != null) {
            result.setMaxInactiveInterval(Duration.ofSeconds(this.defaultMaxInactiveInterval));
        }
        return result;
    }

    public void setDefaultMaxInactiveInterval(int defaultMaxInactiveInterval) {
        this.defaultMaxInactiveInterval = defaultMaxInactiveInterval;
    }

    public void addTheadSession(Long id, String sessionId){
        threadSessions.put(id, sessionId);
    }
}
