import matplotlib.pyplot as plt
import pandas as pd
import csv
import numpy as np
from sklearn.svm import SVC
from os import listdir
from os.path import isfile, join
from scipy import signal
from scipy import integrate


class Worker():
    def __init__(self, main_path, path,weight = 80):
        self.action_to_label = {'forehand': 0, 'backhand': 1, 'f': 0, 'b': 1}
        self.path = path
        self.title = path.split('.')[0]
        self.main_path = main_path
        self.data = self._preprocess_data()
        self.labels = self._get_labels()
        self.weight = weight
        self.MET = 5

    def _is_float(self, element) -> bool:
        try:
            float(element)
            return True
        except ValueError:
            return False

    def _norm(self, row):
        return (float(row['X']) ** 2 + float(row['Y']) ** 2 + float(row['Z']) ** 2) ** 0.5

    def _get_labels(self):
        if 'backhand' in self.path:
            return np.array(self.action_to_label['backhand'])
        elif 'forehand' in self.path:
            return np.array(self.action_to_label['forehand'])
        else:
            encoded_labels = []
            labels = list(self.title)
            for label in labels:
                encoded_labels.append(self.action_to_label[label])
            return np.array(encoded_labels)

    def _preprocess_data(self):
        full_path = self.main_path + '/' + self.path
        bad_rows = []
        with open(full_path, 'r') as file:
            reader = csv.reader(file)
            for i, row in enumerate(reader):
                for j in range(4):
                    if not self._is_float(row[j]):
                        bad_rows.append(i)
        data = pd.read_csv(full_path, names=['Time', 'X', 'Y', 'Z'])
        data = data.drop(bad_rows)
        for col in data.columns:
            data[col] = data[col].astype(float)
        data['Norm'] = data.apply(self._norm, axis=1)
        data['Time'] -= min(data['Time'])
        data = self._smooth_data(data,['X','Y','Z'],window_size=5)
        data = self._smooth_data(data, ['Norm'], window_size=20)
        data = data.dropna()
        data = data.reset_index()
        return data

    def _smooth_data(self,data, cols, window_size=20):
        for col in cols:
            data[col] = data[col].rolling(window_size, center=True).mean()
        return data

    def _integrate(self, data):
        pass

    def extract_features(self, data):
        pass

    def plot_cols(self,data, cols):
        for col in cols:
            plt.plot(data[col], label=col)

    def plot_points(self, points, label=None):
        if label is None:
            plt.scatter(points)
        else:
            plt.scatter(points, label=label)

    def show_plot(self):
        plt.legend()
        plt.title(self.title)
        plt.show()

    def return_stats(self):
        stats = {}
        stats['Time'] = self._calculate_session_time()
        stats['Number of hits'] = str(self._estimate_number_of_hits())
        stats['Calories burned'] = f"{self._estimate_calories_burned():.2f}"
        stats['Max speed'] = f"{self._estimate_max_speed():.2f}"
        stats['Max acceleration'] = f"{self._estimate_max_acceleration():.2f}"
        #stats['Percentage backhand'],stats['Percentage forehand'] = self._classify_shots()
        return stats

    def _calculate_session_time(self):
        time = max(self.data['Time'])
        minutes = time // 60
        seconds = time % 60
        if seconds < 10:
            return f"{int(minutes)}:0{int(seconds)}"
        else:
            return f"{int(minutes)}:{int(seconds)}"
    def _estimate_number_of_hits(self):
        data = self.data.copy()
        data['Norm'] -= data['Norm'].mean()
        cutoff = max(data['Norm'].std(), 1)
        peaks = signal.find_peaks(data['Norm'], height=cutoff, distance=1)[0]
        final_peaks = []
        crossed_down = True
        for i in range(len(data)):
            if crossed_down:
                if i in peaks:
                    final_peaks.append(i)
                    crossed_down = False
            else:
                if data.loc[i]['Norm'] < 0:
                    crossed_down = True
        return len(final_peaks)

    def _estimate_calories_burned(self):
        return (self.weight * self.MET*max(self.data['Time'])/60)/200

    def _estimate_max_speed(self):

        a_x = integrate.cumtrapz(self.data['X'],self.data['Time'])
        a_y = integrate.cumtrapz(self.data['Y'],self.data['Time'])
        a_z = integrate.cumtrapz(self.data['Z'],self.data['Time'])
        v = np.sqrt(a_x**2+a_y**2+a_z**2)
        return max(v)

    def _estimate_max_acceleration(self):
        return max(self.data['Norm'])

    def _classify_shots(self):
        pass

    def get_copy(self):
        return self.data.copy()






def main():
    main_path = "C:/Users/yoni/Desktop/מסמכים ללימודים/סמסטר ו/IOT/tennis measures/taged"
    #main_path = "C:/Users/yoni/Desktop/מסמכים ללימודים/סמסטר ו/IOT/tennis measures/single shot"
    files = [f for f in listdir(main_path) if isfile(join(main_path, f))]
    for file in files:
        print(file)
        worker = Worker(main_path,file)
        print(worker.return_stats())


def extract_features(data):
    corr_to_norm = data.corr()['Norm'][['X', 'Y', 'Z']].values
    return corr_to_norm


def single_shot():
    main_path = "C:/Users/yoni/Desktop/מסמכים ללימודים/סמסטר ו/IOT/tennis measures/single shot"
    files = [f for f in listdir(main_path) if isfile(join(main_path, f))]
    files_data = []
    labels = []
    for path in files:
        if 'backhand' in path:
            title = 'backhand'
            labels.append(0)
        else:
            title = 'forehand'
            labels.append(1)
        data = clean_data(main_path + '/' + path)
        labels = get_labels(path)
        data = preprocess_data(data)
        for col in ['Norm', 'X', 'Y', 'Z']:
            data[col] = data[col].rolling(20, center=True).mean()

        data['X_int'] = data['X'].apply(lambda g: integrate.trapz(g['X'], x=g['Time']))
        plt.plot(data['Time'], data['Norm'], label='Norm')
        plt.plot(data['Time'], data['X'], label='X')
        plt.plot(data['Time'], data['Y'], label='Y')
        plt.plot(data['Time'], data['Z'], label='Z')
        plt.legend()
        plt.title(title)
        plt.show()
        features = extract_features(data[['X', 'Y', 'Z', 'Norm']])
def multi_shot():
    main_path = "C:/Users/yoni/Desktop/מסמכים ללימודים/סמסטר ו/IOT/tennis measures/taged"
    files = [f for f in listdir(main_path) if isfile(join(main_path, f))]
    for path in files:
        data = clean_data(main_path + '/' + path)
        data = preprocess_data(data)
        for col in ['Norm', 'X', 'Y', 'Z']:
            data[col] = data[col].rolling(20, center=True).mean()
        cutoff = max(data['Norm'].std(), 1)
        peaks = signal.find_peaks(data['Norm'], height=cutoff, distance=1)[0]
        final_peaks = []
        crossed_down = True
        for i in range(len(data)):
            if crossed_down:
                if i in peaks:
                    final_peaks.append(i)
                    crossed_down = False
            else:
                if data.loc[i]['Norm'] < 0:
                    crossed_down = True

        peak_points = np.array([data.loc[i][['Time', 'Norm']].values for i in final_peaks])
        plt.scatter(peak_points[:, 0], peak_points[:, 1], label='peak')
        plt.plot(data['Time'], data['Norm'], label='Norm')
        plt.plot(data['Time'], data['X'], label='X')
        plt.plot(data['Time'], data['Y'], label='Y')
        plt.plot(data['Time'], data['Z'], label='Z')
        plt.title(path.split('.')[0])
        plt.legend()
        plt.show()





if __name__ == "__main__":
    main()
