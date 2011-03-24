/**
* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements.  See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership.  The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License.  You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.apache.hadoop.mapreduce.v2.app.webapp;

import com.google.inject.Inject;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.hadoop.mapreduce.v2.app.job.Task;
import org.apache.hadoop.mapreduce.v2.util.MRApps;
import org.apache.hadoop.util.StringUtils;
import org.apache.hadoop.yarn.webapp.hamlet.Hamlet;
import org.apache.hadoop.yarn.webapp.hamlet.Hamlet.*;
import org.apache.hadoop.yarn.webapp.view.HtmlBlock;

import org.apache.hadoop.mapreduce.v2.api.TaskReport;
import org.apache.hadoop.mapreduce.v2.api.TaskType;

import static org.apache.hadoop.mapreduce.v2.app.webapp.AMWebApp.*;
import static org.apache.hadoop.yarn.util.StringHelper.*;
import static org.apache.hadoop.yarn.webapp.view.JQueryUI.*;

public class TasksBlock extends HtmlBlock {
  final App app;
  final SimpleDateFormat dateFormat =
      new SimpleDateFormat("d-MMM-yyyy HH:mm:ss");

  @Inject TasksBlock(App app) {
    this.app = app;
  }

  @Override protected void render(Block html) {
    if (app.job == null) {
      html.
        h2($(TITLE));
      return;
    }
    TaskType type = null;
    String symbol = $(TASK_TYPE);
    if (!symbol.isEmpty()) {
      type = MRApps.taskType(symbol);
    }
    TBODY<TABLE<Hamlet>> tbody = html.
      table("#tasks").
        thead().
          tr().
            th("Task").
            th("Progress").
            th("State").
            th("Start Time").
            th("Finish Time").
            th("Elapsed Time")._()._().
        tbody();
    for (Task task : app.job.getTasks().values()) {
      if (type != null && task.getType() != type) {
        continue;
      }
      String tid = MRApps.toString(task.getID());
      TaskReport report = task.getReport();
      String pct = percent(report.progress);
      long elapsed = Times.elapsed(report.startTime, report.finishTime);
      tbody.
        tr().
          td().
            br().$title(String.valueOf(task.getID().id))._(). // sorting
            a(url("task", tid), tid)._().
          td().
            br().$title(pct)._().
            div(_PROGRESSBAR).
              $title(join(pct, '%')). // tooltip
              div(_PROGRESSBAR_VALUE).
                $style(join("width:", pct, '%'))._()._()._().
          td(report.state.toString()).
          td().
            br().$title(String.valueOf(report.startTime))._().
            _(dateFormat.format(new Date(report.startTime)))._().
          td().
            br().$title(String.valueOf(report.finishTime))._().
            _(dateFormat.format(new Date(report.finishTime)))._().
          td().
            br().$title(String.valueOf(elapsed))._().
            _(StringUtils.formatTime(elapsed))._()._();
    }
    tbody._()._();
  }
}