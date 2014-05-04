
$(document).ready(function() {
    var DrillDown = {
        setupRequestGraph: function(data) {
            var spaceRestricted = (data[0].values.length > 10 ? true : false);

            nv.addGraph(function() {
                var chart = nv.models.discreteBarChart()
                        .x(function(d) {
                            return d.label;
                        })
                        .y(function(d) {
                            return d.value;
                        })
                        .valueFormat(d3.format(',.0f'))
                        .staggerLabels(true)
                        .tooltips(spaceRestricted)
                        .showValues(!spaceRestricted)
                        .transitionDuration(350);
                chart.yAxis.tickFormat(d3.format(',.0f'));

                d3.select('#chart1 svg')
                        .datum(data)
                        .call(chart);

                if (spaceRestricted) {
                    var updateOrientation = function() {
                        var xTicks = d3.select('.nv-x.nv-axis > g').selectAll('g');
                        xTicks
                                .selectAll('text')
                                .attr('transform', function(d, i, j) {
                                    return 'translate (-10, 25) rotate(-90 0,0)';
                                });
                    };

                    var originalUpdate = chart.update;
                    chart.update = function() {
                        originalUpdate();
                        updateOrientation();
                    };

                    updateOrientation();
                }
                nv.utils.windowResize(chart.update);
                return chart;
            });

        }
    };

    $('.selectpicker').selectpicker();
    $(".searchForm").ajaxForm({
        success: function(obj) {
            DrillDown.setupRequestGraph(obj.requestCountData);
            $.event.trigger({
                type: "cartDataChanged",
                message: obj.sunBurstData,
                time: new Date()
            });
            $("#totalRequestCount").html(obj.totalCount);
        }
    });


    var data = JSON.parse(document.getElementById("data").innerHTML);
    DrillDown.setupRequestGraph(data);
});
 