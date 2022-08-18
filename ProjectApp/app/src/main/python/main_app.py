import pandas as pd
import csv
from scipy import signal
import numpy as np
import joblib
# import ahrs

class Step_analyzer():
    def __init__(self, classifier_path=None):
        if classifier_path is not None:
            self.activity_classifier = joblib.load(classifier_path)
        self.activity_to_idx = {'walk': 0, 'run': 1}
        self.idx_to_activity = {0: 'Walk', 1: 'Run'}

    def preprocess_data(self, path):
        meta_data = {}
        with open(path, 'r') as file:
            csv_reader = csv.reader(file)
            for i, row in enumerate(csv_reader):
                if i == 0:
                    meta_data['name'] = row[1]
                elif i == 1:
                    meta_data['time'] = row[1]
                elif i == 2:
                    if 'run' in path:
                        meta_data['activity'] = self.activity_to_idx['run']
                    else:
                        meta_data['activity'] = self.activity_to_idx['walk']
                elif i == 3:
                    meta_data['steps'] = int(row[1])
                elif len(row) == 0:
                    continue
                elif self.is_float(row[0]):
                    break
        data = pd.read_csv(path, skiprows=i, names=['Time', 'X', 'Y', 'Z'])
        data['Norm'] = data.apply(self.norm, axis=1)
        data = self.anomalies_detection(data)
        data['Norm'] -= data['Norm'].mean()
        data = self.fix_time(data)
        return data, meta_data

    def fix_time(self, data):
        data['Time'] -= min(data['Time'])
        if max(data['Time']) > 10000:
            data['Time'] /= 1000
        return data

    def is_float(self, element) -> bool:
        try:
            float(element)
            return True
        except ValueError:
            return False

    def norm(self, row):
        return (float(row['X']) ** 2 + float(row['Y']) ** 2 + float(row['Z']) ** 2) ** 0.5

    def estimate_steps(self, data, max_=None, min_=None):
        if max_ is not None:
            data = data[data['Time'] <= max_]
        if min_ is not None:
            data = data[data['Time'] <= min_]
        cut_off = max(data['Norm'].std(), 1)
        estimated_steps = len(signal.find_peaks(data['Norm'], cut_off, distance=4)[0])
        return estimated_steps

    def anomalies_detection(self, data):
        q1_pc1, q3_pc1 = data['Norm'].quantile([0.1, 0.9])
        iqr_pc1 = q3_pc1 - q1_pc1
        lower_pc1 = q1_pc1 - (1.5 * iqr_pc1)
        upper_pc1 = q3_pc1 + (1.5 * iqr_pc1)
        data = data[((data['Norm'] <= upper_pc1) & (data['Norm'] >= lower_pc1))]
        return data

    def extract_features(self, data):
        estimated_steps = self.estimate_steps(data)
        steps_per_second = estimated_steps / max(data['Time'])
        return np.array([data['X'].mean(), data['Y'].mean(), data['Z'].mean(),
                         data['Norm'].max(), data['Norm'].min(), steps_per_second]).reshape((1, -1))

    def walk_or_run(self, data):
        features = self.extract_features(data)
        return self.activity_classifier.predict(features)[0]

    def estimate_new(self, path):
        data, meta_data = self.preprocess_data(path)
        estimated_activity = self.idx_to_activity[self.walk_or_run(data)]
        estimated_steps = self.estimate_steps(data)
        return str(estimated_activity)+ '~' + str(estimated_steps)


def main(path):
    main_path = "/storage/self/primary/svm_params/"
    analyzer = Step_analyzer(main_path+'activity_classifier.joblib.pkl')

    return analyzer.estimate_new(path)

