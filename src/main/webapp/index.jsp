<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>

<html>
    <head>
        <meta charset="utf-8">
        <meta http-equiv="X-UA-Compatible" content="IE=edge">
        <meta name="viewport" content="width=device-width, initial-scale=1">

        <!-- Bootstrap -->
        <link href="css/bootstrap.min.css" rel="stylesheet">
        <link href="css/bootstrap-select.min.css" rel="stylesheet">

        <title>Data analysis</title>
        <link rel="stylesheet" href="css/nv.d3.css"/>
        <link rel="stylesheet" href="css/sequences.css" />

        <style>
            .searchForm {
                max-width: 700px;
            }
            body {
                *overflow-y:scroll;
            }

            text {
                *font: 12px sans-serif;
            }
            #chart1 {
                height: 300px;
                *width: 500px;
                margin: 10px;
                min-width: 100px;
                min-height: 100px;
                /*
                  Minimum height and width is a good idea to prevent negative SVG dimensions...
                  For example width should be =< margin.left + margin.right + 1,
                  of course 1 pixel for the entire chart would not be very useful, BUT should not have errors
                */
            }
        </style>
    </head>
    <body>
        <!-- SOLR query used: ${query} -->
        <div class="container">
            <div class="row">
                <div class="well">
                    <form class="form-horizontal searchForm" action="/search" method="POST" role="form">
                        <div class="form-group">
                            <label class="col-sm-4 control-label">Time range</label>
                            <div class="col-sm-8">
                                <select name="timeRange" class="selectpicker form-control">
                                    <option selected="selected" value="LAST_WEEK">Last week</option>
                                    <option value="LAST_MONTH">Last month</option>
                                </select>
                            </div>
                        </div>
                        <div class="form-group">
                            <label for="requestLike" class="col-sm-4 control-label">Request contains</label>
                            <div class="col-sm-8">
                                <input type="text" name="requestLike" class="form-control" id="requestLike">
                            </div>
                        </div>
                        <div class="form-group">
                            <div class="col-sm-10 col-sm-offset-4">
                                <button type="submit" class="btn btn-primary">
                                    Search
                                </button>
                            </div>
                        </div>
                    </form>
                </div>
            </div>
            <div class="row">
                <div class="col-lg-6">
                    <div id="chart1" class='with-3d-shadow with-transitions'>
                        <svg></svg>
                    </div>
                </div>
                <div id="requestSunBurst" class="col-lg-6">
                    <!--<div id="main">-->
                        <div id="sequence"></div>
                        <div class="chart" chart="requestSunBurst">
                            <!--<div id="explanation" style="visibility: hidden;">
                                <span id="percentage"></span><br/>
                                
                            </div>-->
                        </div>
                    <!--</div>-->
                    <!--<div id="sidebar">
                        <input type="checkbox" id="togglelegend"> Legend<br/>
                        <div id="legend" style="visibility: hidden;"></div>
                    </div>-->
                </div>
            </div>
        </div>

        <script id="data" type="application/json">
            ${requestCountData}
        </script>

        <script id="sunBurstData" type="application/json">
            ${sunBurstData}
        </script>
    </body>
    <!-- jQuery (necessary for Bootstrap's JavaScript plugins) -->
    <script src="https://ajax.googleapis.com/ajax/libs/jquery/1.11.0/jquery.min.js"></script>
    <script src="http://ajax.aspnetcdn.com/ajax/jquery.validate/1.11.1/jquery.validate.min.js"></script>

    <script src="js/bootstrap.min.js"></script>
    <script src="js/bootstrap-select.min.js"></script>
    <script src="js/jquery.form.min.js"></script>

    <script type="text/javascript" src="js/d3.v3.js"></script>
    <script type="text/javascript" src="js/nv.d3.js"></script>
    <script type="text/javascript" src="js/tooltip.js"></script>
    <script type="text/javascript" src="js/utils.js"></script>
    <script type="text/javascript" src="js/models/legend.js"></script>
    <script type="text/javascript" src="js/models/axis.js"></script>
    <script type="text/javascript" src="js/models/discreteBar.js"></script>
    <script type="text/javascript" src="js/models/discreteBarChart.js"></script>
    <script type="text/javascript" src="js/drilldown.js"></script>

    <script type="text/javascript" src="js/sequences.js"></script>

    <script type="text/javascript">
        var sunBurstData = JSON.parse(document.getElementById("sunBurstData").innerHTML);
        initSunBurst(window);
        var sunBurst = new SunBurst("requestSunBurst", sunBurstData);
        nv.utils.windowResize(function() { sunBurst.refresh() });
        $(document).on("cartDataChanged", function(e) {
            sunBurst.setData(e.message);
        });
    </script> 

</html>
