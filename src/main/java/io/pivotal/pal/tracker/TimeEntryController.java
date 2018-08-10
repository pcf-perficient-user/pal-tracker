package io.pivotal.pal.tracker;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.metrics.CounterService;
import org.springframework.boot.actuate.metrics.GaugeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.sql.Time;
import java.util.List;

@RestController()
@RequestMapping("/time-entries")
public class TimeEntryController {
    private TimeEntryRepository timeEntryRepository;
    private final CounterService counter;
    private final GaugeService gauge;
    //public TimeEntryController(TimeEntryRepository timeEntryRepository) {
    //  this.timeEntryRepository=timeEntryRepository;
    //}
    public TimeEntryController(TimeEntryRepository timeEntryRepository,
                               CounterService counter,
                               GaugeService gauge) {
        this.timeEntryRepository=timeEntryRepository;
        this.counter = counter;
        this.gauge = gauge;
    }

    @PostMapping
    public ResponseEntity create(@RequestBody TimeEntry timeEntryToCreate) {
        TimeEntry timeEntryCreated=timeEntryRepository.create(timeEntryToCreate);
        counter.increment("TimeEntry.created");
        gauge.submit("timeEntries.count", timeEntryRepository.list().size());
        return ResponseEntity.created(URI.create("/create")).header("MyResponseHeader", "MyValue").body(timeEntryCreated);
    }

    @GetMapping("{id}")
    public ResponseEntity<TimeEntry> read(@PathVariable Long id) {
        TimeEntry timeEntryToFind=timeEntryRepository.find(id);
        if (timeEntryToFind!=null) {
            counter.increment("TimeEntry.read");
            return ResponseEntity.ok().body(timeEntryToFind);
        }
        else return ResponseEntity.notFound().build();
    }

    @GetMapping
    public ResponseEntity<List<TimeEntry>> list() {
        List<TimeEntry> list=timeEntryRepository.list();
        counter.increment("TimeEntry.listed");
        return ResponseEntity.ok().body(list);
    }

    @PutMapping("{id}")
    public ResponseEntity update(@PathVariable long id, @RequestBody TimeEntry timeEntry) {
        TimeEntry timeEntryUpdated=timeEntryRepository.update(id, timeEntry);
        if (timeEntryUpdated!=null) {
            counter.increment("TimeEntry.updated");
            return ResponseEntity.ok().body(timeEntryUpdated);
        }
        else return ResponseEntity.notFound().build();
    }

    @DeleteMapping("{id}")
    public ResponseEntity<TimeEntry> delete(@PathVariable long id) {
        //if (timeEntryRepository.find(id)!=null) {
        timeEntryRepository.delete(id);
        counter.increment("TimeEntry.deleted");
        gauge.submit("timeEntries.count", timeEntryRepository.list().size());
        return ResponseEntity.noContent().build();
    //}
        //else return ResponseEntity.notFound().build();
    }
}
