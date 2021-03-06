/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.impl.clients;

import com.google.common.base.Preconditions;
import java.io.File;
import java.io.IOException;
import java.util.SortedMap;
import java.util.TreeMap;
import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import org.opendaylight.openflowjava.util.ByteBufUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

/**
 * Implementation of ScenarioService.
 *
 * @author Jozef Bacigal
 */
public class ScenarioServiceImpl implements ScenarioService {

    private static final Logger LOG = LoggerFactory.getLogger(ScenarioServiceImpl.class);

    private String xmlFilePathWithFileName = SIMPLE_CLIENT_SRC_MAIN_RESOURCES + SCENARIO_XML;

    public ScenarioServiceImpl(String scenarioFile) {
        if (null != scenarioFile && !scenarioFile.isEmpty()) {
            this.xmlFilePathWithFileName = scenarioFile;
        }
    }

    @Override
    public Scenario unMarshallData(String scenarioName) throws SAXException, JAXBException {
        SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Schema schema = sf.newSchema(new File(XSD_SCHEMA_PATH_WITH_FILE_NAME));
        LOG.debug("Loading schema from: {}", XSD_SCHEMA_PATH_WITH_FILE_NAME);

        JAXBContext jc = JAXBContext.newInstance(Scenarios.class);

        Unmarshaller unmarshaller = jc.createUnmarshaller();
        unmarshaller.setSchema(schema);

        Scenarios scenarios = (Scenarios) unmarshaller.unmarshal(new File(xmlFilePathWithFileName));
        LOG.debug("Scenarios ({}) are un-marshaled from {}", scenarios.getScenario().size(),
                xmlFilePathWithFileName);

        boolean foundConfiguration = false;
        Scenario scenarioType = null;
        for (Scenario scenario : scenarios.getScenario()) {
            if (scenario.getName().equals(scenarioName)) {
                scenarioType = scenario;
                foundConfiguration = true;
            }
        }
        if (!foundConfiguration) {
            LOG.warn("Scenario {} not found.", scenarioName);
        } else {
            LOG.info("Scenario {} found with {} steps.", scenarioName, scenarioType.getStep().size());
        }
        return scenarioType;
    }

    @Override
    public SortedMap<Integer, ClientEvent> getEventsFromScenario(Scenario scenario) throws IOException {
        Preconditions.checkNotNull(scenario, "Scenario name not found. Check XML file, scenario name or directories.");
        SortedMap<Integer, ClientEvent> events = new TreeMap<>();
        Integer counter = 0;
        for (Step step : scenario.getStep()) {
            LOG.debug("Step {}: {}, type {}, bytes {}", step.getOrder(), step.getName(), step.getEvent().value(),
                    step.getBytes().toArray());
            switch (step.getEvent()) {
                case SLEEP_EVENT:
                    events.put(counter++, new SleepEvent(1000));
                    break;
                case SEND_EVENT:
                    events.put(counter++, new SendEvent(ByteBufUtils.serializeList(step.getBytes())));
                    break;
                case WAIT_FOR_MESSAGE_EVENT:
                    events.put(counter++, new WaitForMessageEvent(ByteBufUtils.serializeList(step.getBytes())));
                    break;
                default:
                    break;
            }
        }
        return events;
    }

}
