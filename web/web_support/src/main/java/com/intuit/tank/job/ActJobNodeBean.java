package com.intuit.tank.job;

/*
 * #%L
 * JSF Support Beans
 * %%
 * Copyright (C) 2011 - 2015 Intuit Inc.
 * %%
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * #L%
 */

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.time.FastDateFormat;

import com.intuit.tank.api.model.v1.cloud.CloudVmStatusContainer;
import com.intuit.tank.project.JobInstance;

public class ActJobNodeBean extends JobNodeBean {

    private static final long serialVersionUID = 1L;
    private List<VMNodeBean> vmBeans = new ArrayList<VMNodeBean>();
    private String jobDetails;

    public ActJobNodeBean(JobInstance job, boolean hasRights, FastDateFormat fmt) {
        super();
        this.setHasRights(hasRights);
        this.setName(job.getName());
        this.setJobId(String.valueOf(job.getId()));
        this.setId(String.valueOf(job.getId()));
        this.setReportMode(job.getReportingMode().toString());
        this.setStatus(job.getStatus().toString());
        this.setRegion("");
        this.setActiveUsers(String.valueOf(job.getBaselineVirtualUsers()));
        this.setTotalUsers(String.valueOf(job.getTotalVirtualUsers()));
        this.jobDetails = job.getJobDetails();
        this.setStartTime(job.getStartTime());
        this.setEndTime(job.getEndTime());
    }

    public ActJobNodeBean(String jobId, CloudVmStatusContainer container, FastDateFormat fmt) {
        super();
        this.setName(jobId);
        this.setJobId(jobId);
        this.setId(jobId);
        this.setReportMode("");
        this.setStatus(container.getStatus().name());
        this.setRegion("");
        this.setActiveUsers("");
        this.setTotalUsers("");
        this.setStartTime(container.getStartTime());
        this.setEndTime(container.getEndTime());
    }

    @Override
    public void reCalculate() {
        this.setTps(calculateTPS());
    }

    private int calculateTPS() {
        return vmBeans.stream().mapToInt(JobNodeBean::getTps).sum();
    }

    public String getJobDetails() {
        return jobDetails;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isDeleteable() {
        return true;
    }

    @Override
    public boolean isJobNode() {
        return true;
    }

    /**
     * @return the jobBeans
     */
    public List<VMNodeBean> getVmBeans() {
        return vmBeans;
    }

    /**
     * @param vmBeans
     *            the jobBeans to set
     */
    public void setVmBeans(List<VMNodeBean> vmBeans) {
        this.vmBeans = vmBeans;
    }

    /**
     * Adds a vmBean to the jobNode
     * 
     * @param vmNode
     *            Node to be added.
     */
    public void addVMBean(VMNodeBean vmNode) {
        getVmBeans().add(vmNode);
    }

    @Override
    public List<VMNodeBean> getSubNodes() {
        return vmBeans;
    }

    @Override
    public boolean hasSubNodes() {
        return vmBeans.size() > 0;
    }

    @Override
    public boolean isKillable() {
        return JobStatusHelper.canBeKilled(getStatus());
    }

    @Override
    public boolean isStoppable() {
        return JobStatusHelper.canBeStopped(getStatus());
    }

    @Override
    public boolean isRunnable() {
        return JobStatusHelper.canBeRun(getStatus());
    }

    @Override
    public boolean isPauseable() {
        return JobStatusHelper.canBePaused(getStatus());
    }

    @Override
    public boolean isRampPauseable() {
        return JobStatusHelper.canRampBePaused(getStatus());
    }

    @Override
    public boolean isDeletable() { return JobStatusHelper.canBeDeleted(getStatus()); }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getType() {
        return "job";
    }

}
