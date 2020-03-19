$(document).ready(function(){

    // config variables
    let solr_url = 'solr/ubo_projects/select/';
    let id_project_acronym = 'project_acronym';
    let id_project_title = 'project_title';
    let id_project = 'project_id';
    let id_funder = 'funder';
    let id_funding_number = 'funding_number';

    let min_length = 0;
    let input_delay = 200;
    let max_label_length = 90;

    // derived settings
    let acronym_input = $('#' + id_project_acronym);
    let title_input = $('#' + id_project_title);
    let project_id_input = $('#' + id_project);
    let funder_input = $('#' + id_funder);
    let funding_number_input = $('#' + id_funding_number);

    let call_solr = function(request, response) {
    	let search_string = request.term.toLowerCase();
        let solr_search_string = 'project_search_all:' + search_string + '*';
        $.ajax({
           url: solr_url,
           dataType: 'jsonp',
           jsonp: 'json.wrf',
           data: {
               q: solr_search_string,
               wt: 'json',
               fl: '*'
           },
           success: function(data) {
            let docs = data.response.docs;
            let sources = [];
            $.each(docs, function(i, doc) {
                let source_label = (doc.acronym + ': ' + doc.project_title).substring(0, max_label_length) + '...';
                sources.push({'label': source_label, 'project_title': doc.project_title, 'acronym': doc.acronym,
                'project_id': doc.project_id, 'funder': doc.funder, 'funding_number': doc.funding_number});
            });
            response(sources);
        }
        });
    }

    let select_project = function(event, ui) {
        acronym_input.val(ui.item.acronym);
        title_input.val(ui.item.project_title);
        project_id_input.val(ui.item.project_id);
        funder_input.val(ui.item.funder);
        funding_number_input.val(ui.item.funding_number);
        return false;
    }

    // autocomplete settings and initialization
    acronym_input.autocomplete({delay: input_delay, minLength: min_length, source: call_solr, select: select_project});
    title_input.autocomplete({delay: input_delay, minLength: min_length, source: call_solr, select: select_project});
    funder_input.autocomplete({delay: input_delay, minLength: min_length, source: call_solr, select: select_project});
    funding_number_input.autocomplete({delay: input_delay, minLength: min_length, source: call_solr, select: select_project});
});