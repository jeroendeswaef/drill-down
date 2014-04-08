// http://bl.ocks.org/kerryrodden/7090426#sequences.js

function initSunBurst(root) {

    function SunBurst(parentElementId, sunBurstData) {
        this.parentElementId = parentElementId;
        this.parentElement = d3.select('#' + parentElementId);
        
        this.width = this.calculateWidth();
        this.height = this.calculateHeight();
        this.sunBurstData = sunBurstData;
        this.redraw();
    }

    SunBurst.prototype.calculateHeight = function() {
           // -70, to make room at the top for the breadcrumbs
            return (this.width * 3 / 5) - 70;
    };
    
    SunBurst.prototype.calculateWidth = function() {
        // Dimensions of sunburst.
        // -30, because the offsetwith doesnt count the padding
        return this.parentElement.node().offsetWidth - 30;
    };
    
    SunBurst.prototype.setData = function(sunBurstData) {
        this.sunBurstData = sunBurstData;
        this.redraw();
    };
    
    // Redraws itself if the size has changed
    SunBurst.prototype.refresh = function() {
        var newWidth = this.calculateWidth();
       
        if (newWidth !== this.width) {
            this.width = newWidth;
            this.height = this.calculateHeight();
            this.redraw();
        }
    };
    
    SunBurst.prototype.redraw = function() {
        var width = this.width;
        var height = this.height;
        var radius = Math.min(width, height) / 2;

        // Breadcrumb dimensions: width, height, spacing, width of tip/tail.
        var b = {
            w: 75, h: 30, s: 3, t: 10
        };

        // Mapping of step names to colors.
        var colors = {
            "200": "#339213",
            "304": "#8441A5",
            "404": "#D60030",
            "get": "#0E0E0E",
            "post": "#EE6000"
        };

        // Total size of all segments; we set this later, after loading the data.
        var totalSize = 0;

        //var vis = parentElement.select(".cart");
        //vis.empty();
        //d3.select("[chart=requestSunBurst]").
        var vis = d3.select("[chart=" + this.parentElementId + "]")
                 .html("").append("svg:svg")
                .attr("width", width)
                .attr("height", height)
                .append("svg:g")
                .attr("id", "container")
                .attr("transform", "translate(" + width / 2 + "," + height / 2 + ")");

        var partition = d3.layout.partition()
                .size([2 * Math.PI, radius * radius])
                .value(function(d) {
                    return d.size;
                });

        var arc = d3.svg.arc()
                .startAngle(function(d) {
                    return d.x;
                })
                .endAngle(function(d) {
                    return d.x + d.dx;
                })
                .innerRadius(function(d) {
                    return Math.sqrt(d.y);
                })
                .outerRadius(function(d) {
                    return Math.sqrt(d.y + d.dy);
                });

        
        createVisualization(this.sunBurstData);

        // Main function to draw and set up the visualization, once we have the data.
        function createVisualization(json) {

            // Basic setup of page elements.
            initializeBreadcrumbTrail();
            //drawLegend();
            //d3.select("#togglelegend").on("click", toggleLegend);

            // Bounding circle underneath the sunburst, to make it easier to detect
            // when the mouse leaves the parent g.
            vis.append("svg:circle")
                    .attr("r", radius)
                    .style("opacity", 0);

            // For efficiency, filter nodes to keep only those large enough to see.
            var nodes = partition.nodes(json)
                    .filter(function(d) {
                        return (d.dx > 0.005); // 0.005 radians = 0.29 degrees
                    });

            var path = vis.data([json]).selectAll("path")
                    .data(nodes)
                    .enter().append("svg:path")
                    .attr("display", function(d) {
                        return d.depth ? null : "none";
                    })
                    .attr("d", arc)
                    .attr("fill-rule", "evenodd")
                    .style("fill", function(d) {
                        return colors[d.name];
                    })
                    .style("opacity", 1)
                    .on("mouseover", mouseover);

            // Add the mouseleave handler to the bounding circle.
            d3.select("#container").on("mouseleave", mouseleave);

            // Get total size of the tree = value of root node from partition.
            totalSize = path.node().__data__.value;
        }
        ;

        // Fade all but the current sequence, and show it in the breadcrumb trail.
        function mouseover(d) {

            var percentage = (100 * d.value / totalSize).toPrecision(3);
            var percentageString = percentage + "%";
            if (percentage < 0.1) {
                percentageString = "< 0.1%";
            }

            d3.select("#percentage")
                    .text(percentageString);

            d3.select("#explanation")
                    .style("visibility", "");

            var sequenceArray = getAncestors(d);
            updateBreadcrumbs(sequenceArray, percentageString);

            // Fade all the segments.
            d3.selectAll("path")
                    .style("opacity", 0.3);

            // Then highlight only those that are an ancestor of the current segment.
            vis.selectAll("path")
                    .filter(function(node) {
                        return (sequenceArray.indexOf(node) >= 0);
                    })
                    .style("opacity", 1);
        }

        // Restore everything to full opacity when moving off the visualization.
        function mouseleave(d) {

            // Hide the breadcrumb trail
            d3.select("#trail")
                    .style("visibility", "hidden");

            // Deactivate all segments during transition.
            d3.selectAll("path").on("mouseover", null);

            // Transition each segment to full opacity and then reactivate it.
            d3.selectAll("path")
                    .transition()
                    .duration(1000)
                    .style("opacity", 1)
                    .each("end", function() {
                        d3.select(this).on("mouseover", mouseover);
                    });

            d3.select("#explanation")
                    .transition()
                    .duration(1000)
                    .style("visibility", "hidden");
        }

        // Given a node in a partition layout, return an array of all of its ancestor
        // nodes, highest first, but excluding the root.
        function getAncestors(node) {
            var path = [];
            var current = node;
            while (current.parent) {
                path.unshift(current);
                current = current.parent;
            }
            return path;
        }

        function initializeBreadcrumbTrail() {
            // Add the svg area.
            var trail = d3.select("#sequence").html("").append("svg:svg")
                    .attr("width", width)
                    .attr("height", 50)
                    .attr("id", "trail");
            // Add the label at the end, for the percentage.
            trail.append("svg:text")
                    .attr("id", "endlabel")
                    .style("fill", "#000");
        }

        // Generate a string that describes the points of a breadcrumb polygon.
        function breadcrumbPoints(d, i) {
            var points = [];
            points.push("0,0");
            points.push(b.w + ",0");
            points.push(b.w + b.t + "," + (b.h / 2));
            points.push(b.w + "," + b.h);
            points.push("0," + b.h);
            if (i > 0) { // Leftmost breadcrumb; don't include 6th vertex.
                points.push(b.t + "," + (b.h / 2));
            }
            return points.join(" ");
        }

        // Update the breadcrumb trail to show the current sequence and percentage.
        function updateBreadcrumbs(nodeArray, percentageString) {

            // Data join; key function combines name and depth (= position in sequence).
            var g = d3.select("#trail")
                    .selectAll("g")
                    .data(nodeArray, function(d) {
                        return d.name + d.depth;
                    });

            // Add breadcrumb and label for entering nodes.
            var entering = g.enter().append("svg:g");

            entering.append("svg:polygon")
                    .attr("points", breadcrumbPoints)
                    .style("fill", function(d) {
                        return colors[d.name];
                    });

            entering.append("svg:text")
                    .attr("x", (b.w + b.t) / 2)
                    .attr("y", b.h / 2)
                    .attr("dy", "0.35em")
                    .attr("text-anchor", "middle")
                    .text(function(d) {
                        return d.name;
                    });

            // Set position for entering and updating nodes.
            g.attr("transform", function(d, i) {
                return "translate(" + i * (b.w + b.s) + ", 0)";
            });

            // Remove exiting nodes.
            g.exit().remove();

            // Now move and update the percentage at the end.
            d3.select("#trail").select("#endlabel")
                    .attr("x", (nodeArray.length + 0.5) * (b.w + b.s))
                    .attr("y", b.h / 2)
                    .attr("dy", "0.35em")
                    .attr("text-anchor", "middle")
                    .text(percentageString);

            // Make the breadcrumb trail visible, if it's hidden.
            d3.select("#trail")
                    .style("visibility", "");

        }
    }
    
    root.SunBurst = SunBurst;
}