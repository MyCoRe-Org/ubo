$(document).ready(function(){

    // config variables
    let solr_url = webApplicationBaseURL + '/servlets/solr/select';
    let class_name = 'author-search';
    let id_connection = 'connection';

    let min_length = 2;
    let input_delay = 200;
    let max_label_length = 30;

    // derived settings
    let name_inputs = $('.' + class_name);
    let connection_input = $('#' + id_connection);

    let call_solr = function(request, response) {
        let search_string = request.term;
        let solr_search_string = '(name:*' + search_string + '* OR name_id_dhsbid:*' + search_string + '*)';
        $.ajax({
           url: solr_url,
           dataType: 'jsonp',
           jsonp: 'json.wrf',
           data: {
               q: "name_id_connection:* AND " + solr_search_string,
               wt: 'json',
               fl: '*',
               rows: 30,
               sort: 'name asc',
               group: true,
               'group.field': 'name'
           },
           success: function(data) {
            let groups = data.grouped.name.groups;
            let sources = [];
            $.each(groups, function(i, group) {
                let doc = group.doclist.docs[0];
                let name = doc.name;
                let connection = doc.name_id_connection;
                if(name && connection) {
                    sources.push({'label': name, 'value': connection});
                }
            });
            response(sources);
        }
        });
    }

    let select_project = function(event, ui) {
        $(this).val(ui.item.label);
        $(this).siblings('#' + id_connection).val(ui.item.value);
        return false;
    }

    // autocomplete settings and initialization
    name_inputs.autocomplete({delay: input_delay, minLength: min_length, source: call_solr, select: select_project});
});
