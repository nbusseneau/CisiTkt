# -*- coding: utf-8 -*-
from __future__ import division

def parse(filename):
    res = {}
    with open(filename) as f:
        for line in f:
            split = line.split()
            query = split[0]
            doc = split[1]
            try:
                res[int(query)].add(int(doc))
            except KeyError:
                res[int(query)] = set([int(doc)])
    return res

def evaluate(results, reference):
    evaluation = {}
    for query in results:
        try:
            inter = len(results[query].intersection(reference[query]))
            precision = inter / len(results[query])
            rappel = inter / len(reference[query])
        except KeyError:
            precision = 0
            rappel = 'Undefined'
        try:
            f_mesure = (2 * precision * rappel) / (precision + rappel)
        except (ZeroDivisionError, TypeError):
            f_mesure = 'Undefined'
        evaluation[query] = [precision, rappel, f_mesure]
    return evaluation
    
def mean_values(evaluation):
    mean_precision, mean_rappel, mean_f_mesure, nb_queries = [0, 0, 0, 0]
    for query in evaluation:
        precision, rappel, f_mesure = evaluation[query]
        if not 'Undefined' in str(f_mesure):
            nb_queries += 1
            mean_precision += precision
            mean_rappel += rappel
            mean_f_mesure += f_mesure
    means = [x / nb_queries for x in [mean_precision, mean_rappel, mean_f_mesure]]
    return means

if __name__ == '__main__':
    # Parse result files
    reference = parse('../Resources/CISI.REL')
    normalized = parse('../Out/relevance_normalized.txt')
    tfdf = parse('../Out/relevance_tfdf.txt')

    # Compute performance and correctness for each query
    eval_normalized = evaluate(normalized, reference)
    eval_tfdf = evaluate(tfdf, reference)

    # Print mean values over all the queries
    print 'Normalisé :\nPrécision moyenne: {}\nRappel moyen: {}\nF-mesure moyenne : {}\n'.format(*mean_values(eval_normalized))
    print 'TFDF :\nPrécision moyenne: {}\nRappel moyen: {}\nF-mesure moyenne : {}\n'.format(*mean_values(eval_tfdf))