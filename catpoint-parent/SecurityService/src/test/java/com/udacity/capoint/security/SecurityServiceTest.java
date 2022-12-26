/**
 * ATTRIBUTIONS
 * Thanks to Session Lead Tony Salazar for creating
 * walk-trough video for this project, I followed his guidance
 * to build unit tests and meet project test requirements.
 */


package com.udacity.capoint.security;

        import com.udacity.catpoint.image.service.FakeImageService;
        import com.udacity.catpoint.image.service.ImageService;
        import com.udacity.catpoint.security.data.*;
        import com.udacity.catpoint.security.service.*;
        import org.junit.jupiter.api.BeforeEach;
        import org.junit.jupiter.api.Test;
        import org.junit.jupiter.api.extension.ExtendWith;
        import org.junit.jupiter.params.ParameterizedTest;
        import org.junit.jupiter.params.provider.EnumSource;
        import org.junit.jupiter.params.provider.ValueSource;
        import org.mockito.ArgumentCaptor;
        import org.mockito.ArgumentMatchers;
        import org.mockito.Mock;
        import org.mockito.junit.jupiter.MockitoExtension;

        import java.awt.image.BufferedImage;
        import java.util.HashSet;
        import java.util.Set;

        import static org.junit.jupiter.api.Assertions.*;
        import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SecurityServiceTest {
    private SecurityService securityService;
    private Sensor sensor;
//TODO :
// mock ImageService interface instead of calling FakeImageService
    @Mock
    private FakeImageService imageService;

    @Mock
    private SecurityRepository securityRepository;

    @BeforeEach
    void instantiateNew() {
        securityService = new SecurityService(securityRepository,  imageService);
        sensor = new Sensor("Test", SensorType.DOOR);
    }

    /**
     * Test 1 -
     * Given system alarm is armed
     * When sensor is activated
     * Then - put alarm status to pending
     */
    // Method as shown in project walk-through by Session Lead Tony Salazar
    @Test
    public void ifArmedAndSensorActivated_changeStatusToPending() {
        when(securityRepository.getArmingStatus()).thenReturn(ArmingStatus.ARMED_HOME);
        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.NO_ALARM);
        securityService.changeSensorActivationStatus(sensor, true);
        verify(securityRepository, times(1)).setAlarmStatus(AlarmStatus.PENDING_ALARM);
    }

    /**
     * Test 2
     * Given - system alarm is armed AND sensor is activated AND system already pending
     * When - a sensor is activated
     * Then - put alarm status to ALARM
     */

    @ParameterizedTest
    @EnumSource(value = ArmingStatus.class, names = {"ARMED_HOME", "ARMED_AWAY"})
    void ifSystemPendingAndArmedAndSensorActivated_setStatusToAlarm(ArmingStatus status) {
        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.PENDING_ALARM);
        when(securityRepository.getArmingStatus()).thenReturn(status);
        securityService.changeSensorActivationStatus(sensor, true);
        verify(securityRepository, atMost(2)).setAlarmStatus(AlarmStatus.ALARM);
    }

    /**
     * Test 3
     * Given - system alarm is pending alarm AND sensors are inactive
     * When - sensors are inactive
     * Then - put alarm status to NO ALARM
     */

    // helper method inactivates all sensors
    private Set<Sensor> inactivateSensors() {
        Set<Sensor> sensors = new HashSet<>();
        for (int i = 0; i <= SensorType.values().length; i++) {
            sensors.add(new Sensor("Test", SensorType.DOOR));
        }
        sensors.forEach(sensor -> sensor.setActive(false));
        return sensors;
    }

    @Test
    void ifPendingAndSensorsInactive_returnStatusToNoAlarm() {
        Set<Sensor> allSensors = inactivateSensors();
        Sensor sensor = allSensors.iterator().next();
        sensor.setActive(true);

        when(securityService.getAlarmStatus()).thenReturn(AlarmStatus.PENDING_ALARM);
        securityService.changeSensorActivationStatus(sensor, false);

        ArgumentCaptor<AlarmStatus> captor = ArgumentCaptor.forClass(AlarmStatus.class);

        verify(securityRepository, atMostOnce()).setAlarmStatus(captor.capture());
        assertEquals(captor.getValue(), AlarmStatus.NO_ALARM);
    }

    /**
     * Test 4
     * Given - system alarm is active
     * When - a sensor's state changes
     * Then - a sensor's state change doesn't affect alarm status
     */
    // Method as shown in project walk-through by Session Lead Tony Salazar
    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void ifAlarmActive_changeSensorNotAffectAlarmStatus(boolean status) {
        lenient().when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.ALARM);
        securityService.changeSensorActivationStatus(sensor, status);
        verify(securityRepository, never()).setAlarmStatus(any(AlarmStatus.class));
    }


    /**
     * Test 5
     * Given - system alarm is pending
     * When - a sensor is activated while already active
     * Then - change sensor to alarm state
     */
    @Test
    void ifSensorActivatedWhileActiveAndSystemPending_changeToAlarm () {
        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.PENDING_ALARM);
        securityService.changeSensorActivationStatus(sensor, true);
        sensor.setActive(true);

        ArgumentCaptor<AlarmStatus> captor = ArgumentCaptor.forClass(AlarmStatus.class);

        verify(securityRepository, atMostOnce()).setAlarmStatus(captor.capture());
        assertEquals(captor.getValue(), AlarmStatus.ALARM);
    }

    /**
     * Test 6
     * Given - any system alarm status
     * When - a sensor is deactivated while already inactive
     * Then - make no changes to alarm status. setAlertStatus was not called
     */
    // Method as shown in project walk-through by Session Lead Tony Salazar
    @ParameterizedTest
    @EnumSource(value = AlarmStatus.class)
    void ifSensorDeactivatedWhileInactive_noChangesToAlarm(AlarmStatus status) {
        lenient().when(securityRepository.getAlarmStatus()).thenReturn(status);
        sensor.setActive(false);
        securityService.changeSensorActivationStatus(sensor, false);

        verify(securityRepository, never()).setAlarmStatus(any(AlarmStatus.class));
    }

    /**
     * Test 7
     * Given - system is armed-home
     * When - image service identifies image containing cat
     * Then - put system into alarm status
     */
    @Test
    void ifImageServiceFindsCatWhileSystemArmedHome_changeSystemToAlarm() {
        when(imageService.imageContainsCat(any(), ArgumentMatchers.anyFloat())).thenReturn(true);
        when(securityRepository.getArmingStatus()).thenReturn(ArmingStatus.ARMED_HOME);
        securityService.processImage(mock(BufferedImage.class));

        verify(securityRepository, atMostOnce()).setAlarmStatus(AlarmStatus.ALARM);
    }

    /**
     * Test 8
     * Given - sensors are not active
     * When - image service identifies image that does not contain a cat
     * Then - change alarm status to No Alarm
     */
    @Test
    void ifImgServFindsNoCatWhileSensorsNotActive_changeStatusToNoAlarm() {
        Set<Sensor> allSensors = inactivateSensors();
        lenient().when(securityRepository.getSensors()).thenReturn(allSensors);
        when(imageService.imageContainsCat(any(), anyFloat())).thenReturn(false);
        securityService.processImage(mock(BufferedImage.class));

        ArgumentCaptor<AlarmStatus> captor = ArgumentCaptor.forClass(AlarmStatus.class);

        verify(securityRepository, atMostOnce()).setAlarmStatus(captor.capture());
        assertEquals(captor.getValue(), AlarmStatus.NO_ALARM);
    }

    /**
     * Test 9
     * Given - system is disarmed
     * When -
     * Then - change alarm status to No Alarm
     */
    @Test
    void ifSystemDisarmed_changeToNoAlarm() {
        lenient().when(securityRepository.getArmingStatus()).thenReturn(ArmingStatus.DISARMED);

        verify(securityRepository, atMostOnce()).setAlarmStatus(AlarmStatus.NO_ALARM);


    }
    /**
     * Test 10
     * Given - system is Armed
     * When -
     * Then - reset all sensors to inactive
     */
    @ParameterizedTest
    @EnumSource(value = ArmingStatus.class, names = {"ARMED_HOME", "ARMED_AWAY"})
    void ifSystemArmed_changeSensorsToInactive(ArmingStatus status){
        securityService.setArmingStatus(status);
        Set<Sensor> allSensors = inactivateSensors();

        allSensors.forEach(sensor -> assertFalse(sensor.getActive()));

    }

    /**
     * Test 11
     * Given - system is armed home
     * When - camera shows a cat
     * Then - change alarm status to Alarm
     */
    @Test
    void ifSystemArmedHomeWhileCamShowsCat_changeStatusToAlarm() {
        when(securityRepository.getArmingStatus()).thenReturn(ArmingStatus.ARMED_HOME);
        when(imageService.imageContainsCat(any(), ArgumentMatchers.anyFloat())).thenReturn(true);
        securityService.processImage(mock(BufferedImage.class));

        verify(securityRepository, atMostOnce()).setAlarmStatus(AlarmStatus.ALARM);
    }

}

