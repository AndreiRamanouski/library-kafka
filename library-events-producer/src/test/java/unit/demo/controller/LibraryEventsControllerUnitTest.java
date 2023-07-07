package demo.controller;

import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.library.demo.LibraryEventsProducerApplication;
import com.library.demo.controller.LibraryEventsController;
import com.library.demo.domain.LibraryEvent;
import com.library.demo.producer.LibraryEventsProducerImpl;
import com.library.demo.util.TestUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;


@WebMvcTest(LibraryEventsController.class)
@ContextConfiguration(classes = LibraryEventsProducerApplication.class)
public class LibraryEventsControllerUnitTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    LibraryEventsProducerImpl libraryEventsProducer;



    @Test
    void postLibraryEvent() throws Exception {
        //given
        String valueAsString = objectMapper.writeValueAsString(TestUtil.libraryEvent());
        //when
        when(libraryEventsProducer.sendLibraryEvent_approach1(isA(LibraryEvent.class)))
                .thenReturn(null);
        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/library-event")
                        .content(valueAsString).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());

        //then

    }


    @Test
    void postLibraryEvent_invalidValues() throws Exception {
        //given
        String expectedErrorMessage = "[book.bookName must not be blank, book.bookId must not be null]";
        String valueAsString = objectMapper.writeValueAsString(TestUtil.libraryEventWithInvalidBook());
        //when
        when(libraryEventsProducer.sendLibraryEvent_approach1(isA(LibraryEvent.class)))
                .thenReturn(null);
        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/library-event")
                        .content(valueAsString).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError()).andExpect(content().string(expectedErrorMessage));

        //then

    }
}

