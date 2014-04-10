# -*- coding: utf-8 -*-
from __future__ import division
from operator import itemgetter
from math import log
from nltk.tokenize import RegexpTokenizer

def count(word, lst):
    """Count number of occurences of word in list."""
    n = 0
    for elem in lst:
        if word == elem:
            n += 1
    return n

class SearchEngine(object):
    """AcquiCo2 search engine implementation."""
    
    def __init__(self):
        """"Constructor."""
        self.tokenizer = RegexpTokenizer('[\w$%]+|[^\s!"#&\'()*+,\-./:;<=>?@[\\\]^_`{|}~]+')
        self.stoplist = []
        self.queries = []
        self.raw_repr_vectors = []
        self.normalized_repr_vectors = []
        self.tfdf_repr_vectors = []

    def stoplist_from_file(self, filename):
        """Build stopwords list from file."""
        with open(filename) as f:
            for line in f:
                self.stoplist.append(line.strip())

    def queries_from_file(self, filename):
        """Build queries list from file."""
        with open(filename) as f:
            first = True
            skip = True
            for line in f:
                if line.startswith('.I') and not first:
                    self.queries.append(s)
                    skip = True
                elif line.startswith('.I') and first:
                    first = False
                    skip = True
                elif line.startswith('.W'):
                    skip = False
                    s = ''
                elif line.startswith('.B') or line.startswith('.A') or line.startswith('.T'):
                    skip = True
                elif skip == False:
                    s += line
        self.number_of_queries = len(self.queries)

    def reverse_file(self, filename):
        """Load reverse file from filename and return it."""
        reverse_file = {}
        with open(filename) as f:
            for line in f:
                token, doclist = line.split(":")
                doclist = doclist.strip().split(" ")
                pair_list = {}
                for pair in doclist:
                    doc_num, freq = pair.split(",")
                    pair_list[int(doc_num)] = float(freq)
                reverse_file[token] = pair_list
        return reverse_file

    def compute_repr(self, query):
        """Compute representation vectors for given query."""
        raw_tokens = self.tokenizer.tokenize(query)
        filtered_tokens = [token.lower() for token in raw_tokens if token.lower() not in self.stoplist]
        l = len(filtered_tokens)

        raw_vect = {}
        normalized_vect = {}
        tfdf_vect = {}

        for token in filtered_tokens:
            if token not in raw_vect:
                raw_vect[token] = count(token, filtered_tokens) / l
            
        max_freq = max(raw_vect.iteritems(), key=itemgetter(1))[1]

        for token in raw_vect:
            normalized_vect[token] = raw_vect[token] / max_freq
            
        for token in raw_vect:
            docs_containing_token = 0
            for query in self.queries:
                if token in query.lower():
                    docs_containing_token += 1
            tfdf_vect[token] = raw_vect[token] * log(self.number_of_queries / docs_containing_token)
            
        return raw_vect, normalized_vect, tfdf_vect
        
    def add_repr_vectors(self, query):
        """Add computed representation vectors for query to vectors lists."""
        raw, normalized, tfdf = self.compute_repr(query)
        self.raw_repr_vectors.append(raw)
        self.normalized_repr_vectors.append(normalized)
        self.tfdf_repr_vectors.append(tfdf)
    
    def search(self, repr_vector, reverse_file, threshold):
        """Match representation vector against reverse file and return list of matching documents."""
        doc_list = {}
        for token in repr_vector:
            if repr_vector[token] >= threshold:
                try:
                    pair_list = reverse_file[token]
                    for doc in pair_list:
                        if doc in doc_list:
                            doc_list[doc] += pair_list[doc]
                        else:
                            doc_list[doc] = pair_list[doc]
                except KeyError:
                    pass
        return doc_list    
    
    def relevance_assessment(self, filename, relations, threshold):
        """Write relations' relevance assesment to filename."""
        with open(filename, 'wb') as f:
            i = 1
            for rel in relations:
                for doc in sorted(rel.iteritems()):
                    if doc[1] >= threshold:
                        f.write("{:<8}{:<8}{:<7.5f}\n".format(i, str(doc[0])+"   ", doc[1]))
                i += 1


if __name__ == '__main__':
    # Initialize and load resources
    engine = SearchEngine()
    engine.stoplist_from_file('../Resources/motsvides.txt')
    engine.queries_from_file('../Resources/CISI.QRY')

    # Build representation vectors
    for query in engine.queries:
        engine.add_repr_vectors(query)
            
    # Load reverse files
    normalized_reverse_file = engine.reverse_file('../Out/reverse_normalized.txt')
    tfdf_reverse_file = engine.reverse_file('../Out/reverse_tfdf.txt')

    # Search
    normalized_relations = []
    for repr_vector in engine.normalized_repr_vectors:
        normalized_relations.append(engine.search(repr_vector, normalized_reverse_file, 0.6))
    

    tfdf_relations = []
    for repr_vector in engine.tfdf_repr_vectors:
        tfdf_relations.append(engine.search(repr_vector, tfdf_reverse_file, 0.05))
        
    # Write relevance assessments
    engine.relevance_assessment('../Out/relevance_normalized.txt', normalized_relations, 1.2)
    engine.relevance_assessment('../Out/relevance_tfdf.txt', tfdf_relations, 0.15)
