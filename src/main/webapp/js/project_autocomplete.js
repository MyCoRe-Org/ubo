$(document).ready(function(){

    // config variables
    let target_solr_core = 'ubo_projects';
    let id_project_acronym = 'project_acronym';
    let id_project_title = 'project_title';
    let min_length = 0;
    let input_delay = 200;

    // derived settings
    let acronym_input = $('#' + id_project_acronym);
    let title_input = $('#' + id_project_title);

    let call_solr = function(request, response) {
    	let search_string = request.term.toLowerCase();
        let solr_search_string = 'project_search_all:' + search_string + '*';
        let solr_call = 'solr/' + target_solr_core + '/select/?q=' + solr_search_string + '&wt=json&fl=*';
        $.getJSON(solr_call, function(data) {
            let docs = data.response.docs;
            let sources = [];
            $.each(docs, function(i, doc) {
                let source_label = doc.acronym + ': ' + doc.projekttitel;
                sources.push({'label': source_label, 'projekttitel': doc.projekttitel, 'acronym': doc.acronym});
            });
            response(sources);
        });
    }

    let select_project = function(event, ui) {
        acronym_input.val(ui.item.acronym);
        title_input.val(ui.item.projekttitel);
        return false;
    }

    // autocomplete settings and initialization
    acronym_input.autocomplete({delay: input_delay, minLength: min_length, source: call_solr, select: select_project});
    title_input.autocomplete({delay: input_delay, minLength: min_length, source: call_solr, select: select_project});
});