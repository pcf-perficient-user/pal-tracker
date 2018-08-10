package io.pivotal.pal.tracker;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.util.List;

import static java.sql.Statement.RETURN_GENERATED_KEYS;

@Repository()
@Primary
public class JdbcTimeEntryRepository implements TimeEntryRepository {
    private JdbcTemplate jdbcTemplate;

    public JdbcTimeEntryRepository(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }
    @Override
    public TimeEntry create(TimeEntry timeEntry) {
        TimeEntry timeEntryCreated;
        final String sql = "insert into time_entries (project_id, user_id, date, hours) values (?,?,?,?)";

        GeneratedKeyHolder generatedKeyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(con -> {
            PreparedStatement preparedStatement = con.prepareStatement(sql, RETURN_GENERATED_KEYS);
            preparedStatement.setLong(1, timeEntry.getProjectId());
            preparedStatement.setLong(2, timeEntry.getUserId());
            preparedStatement.setDate(3, Date.valueOf(timeEntry.getDate()));
            preparedStatement.setInt(4, timeEntry.getHours());
            return preparedStatement;
        }, generatedKeyHolder);


        Number id = generatedKeyHolder.getKey();
        timeEntry.setId(id.longValue());
        return timeEntry;
    }
    private final RowMapper<TimeEntry> mapper = (rs, rowNum) -> new TimeEntry(
            rs.getLong("id"),
            rs.getLong("project_id"),
            rs.getLong("user_id"),
            rs.getDate("date").toLocalDate(),
            rs.getInt("hours"));
    private final ResultSetExtractor<TimeEntry> extractor = (rs) -> rs.next() ? mapper.mapRow(rs, 1): null;

    @Override
    public TimeEntry find(long id) {
        final String sql = "SELECT id, project_id, user_id, date, hours from time_entries where id=?";
        return jdbcTemplate.query(sql, new Object[]{id},extractor);

    }

    @Override
    public List<TimeEntry> list() {
        final String sql = "SELECT id, project_id, user_id, date, hours from time_entries";
        return jdbcTemplate.query(sql, mapper);
    }

    @Override
    public TimeEntry update(long id, TimeEntry timeEntry) {
        jdbcTemplate.update("UPDATE time_entries " +
                        "SET project_id = ?, user_id = ?, date = ?,  hours = ? " +
                        "WHERE id = ?",
                timeEntry.getProjectId(),
                timeEntry.getUserId(),
                Date.valueOf(timeEntry.getDate()),
                timeEntry.getHours(),
                id);

        return find(id);
    }

    @Override
    public void delete(long id) {
        jdbcTemplate.update("DELETE FROM time_entries WHERE id = ?", id);
    }
}
