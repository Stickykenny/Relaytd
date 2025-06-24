package me.stky.relaytd.services;


import me.stky.relaytd.api.model.Astre;
import me.stky.relaytd.api.model.AstreID;
import me.stky.relaytd.api.repository.AstreRepository;
import me.stky.relaytd.api.service.AstreServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
public class AstreServiceTest {

    @InjectMocks
    private AstreServiceImpl astreService;

    @Mock
    private AstreRepository astreRepository;

    @BeforeEach
    void init() {
        astreService = new AstreServiceImpl(astreRepository);
    }

    @Test
    void testSaveAstre_AstreAlreadyPresentInDB() {
        int number = 1;
        Astre AstreInDB = createAstre(number);
        Astre newAstre = createAstre(number);

        AstreID astreID = newAstre.getAstreID();
        when(astreRepository.findById(astreID)).thenReturn(Optional.of(AstreInDB));

        var result = astreService.saveAstre(newAstre);
        assertEquals(Optional.empty(), result);
    }

    @Test
    void testSaveAstre_NewAstre() {
        int number = 1;
        Astre astre = createAstre(number);
        Optional<Astre> expectedAstre = Optional.of(createAstre(number));

        AstreID astreID = astre.getAstreID();
        when(astreRepository.findById(astreID)).thenReturn(Optional.empty());
        when(astreRepository.save(astre)).thenReturn(astre);

        var result = astreService.saveAstre(astre);
        assertEquals(expectedAstre, result);
    }

    @Test
    void testUpdateAstre_Nullcheck1() {
        Astre astre = createAstre(1);
        AstreID newID = astre.getAstreID();

        assertThrows(NullPointerException.class, () -> astreService.updateAstreID(null, newID));
    }

    @Test
    void testUpdateAstre_Nullcheck2() {
        Astre astre = createAstre(1);
        AstreID oldID = astre.getAstreID();

        assertThrows(NullPointerException.class, () -> astreService.updateAstreID(oldID, null));
    }

    @Test
    void testUpdateAstre_notFound() {
        Astre astre = createAstre(1);
        AstreID oldID = astre.getAstreID();
        AstreID newID = createAstreID(2);


        when(astreRepository.findById(oldID)).thenReturn(Optional.empty());
        Optional<Astre> result = astreService.updateAstreID(oldID, newID);

        assertEquals(Optional.empty(), result);
    }

    @Test
    void testUpdateAstre_newIDAlreadyUsed() {
        Astre astre = createAstre(1);
        AstreID oldID = astre.getAstreID();
        Astre astreDB = createAstre(2);
        AstreID newID = astreDB.getAstreID();


        when(astreRepository.findById(oldID)).thenReturn(Optional.of(astre));
        when(astreRepository.findById(newID)).thenReturn(Optional.of(astreDB));
        Optional<Astre> result = astreService.updateAstreID(oldID, newID);

        assertEquals(Optional.empty(), result);
    }

    @Test
    void testUpdateAstre() {
        Astre astre = createAstre(1);
        AstreID oldID = astre.getAstreID();
        Astre astreDB = createAstre(2);
        AstreID newID = astreDB.getAstreID();
        Astre astreCopy = astreDB.clone();


        when(astreRepository.findById(oldID)).thenReturn(Optional.of(astre));
        when(astreRepository.findById(newID)).thenReturn(Optional.empty());
        when(astreRepository.save(astreCopy)).thenReturn(astreCopy);


        Optional<Astre> result = astreService.updateAstreID(oldID, newID);
        assertEquals(Optional.of(astreDB), result);
    }

    @Test
    void testDeleteAstre() {
        String type = "type1";
        String name = "name1";

        when(astreRepository.findById(new AstreID(type, name))).thenReturn(Optional.empty());

        var result = astreService.deleteAstre(type, name);
        assertTrue(result);
    }

    private Astre createAstre(int number) {
        return new Astre(createAstreID(number),
                "tag1,tag2", "description", "no-parent",
                LocalDate.now(), LocalDate.now(), Boolean.FALSE);
    }

    private AstreID createAstreID(int number) {
        return new AstreID("type1", "name" + number);
    }
}
